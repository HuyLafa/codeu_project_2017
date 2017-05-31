package controllers;

import codeu.chat.common.Conversation;
import codeu.chat.common.NetworkCode;
import codeu.chat.util.Logger;
import codeu.chat.util.Serializers;
import codeu.chat.util.Uuid;

import play.data.DynamicForm;
import play.data.FormFactory;
import play.mvc.*;
import play.db.Database;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.event.LoggingAdapter;
import akka.japi.Pair;
import akka.japi.pf.PFBuilder;
import akka.stream.Materializer;
import akka.stream.javadsl.*;
import akka.event.Logging;
import play.libs.F;
import play.mvc.Controller;

import javax.inject.Inject;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.HashMap;

import views.html.chat;
import models.ChatMessage;
import models.DBUtility;

/**
 * A chat client using WebSocket.
 */
public class ChatController extends Controller {

  // maps a room ID to the user flow for that room
  private HashMap<String,Flow<String, String, NotUsed>> flowMap = new HashMap<>();
  private static final Logger.Log LOG = Logger.newLog(ChatController.class);
  private ActorSystem actorSystem;
  private Materializer mat;
  private Database db;
  @Inject FormFactory formFactory;
  @Inject private play.Environment environment;

  @Inject
  public ChatController(ActorSystem actorSystem, Materializer mat, Database db) {
    this.actorSystem = actorSystem;
    this.mat = mat;
    this.db = db;
    createInitialHubs();
  }

  public Result index() {
    return redirect(routes.ChatController.chatroom("public"));
  }

  public Result chatroom(String roomName) {
    if (session("username") == null) {
      return redirect(routes.LoginController.display());
    }

    // get the websocket URL
    Http.Request request = request();
    String url = routes.ChatController.websocket(roomName).webSocketURL(request);

    // get past messages from the database
    ArrayList<ChatMessage> messages = DBUtility.getAllMessages(db, roomName);
    return ok(chat.render(session("username"), url, flowMap.keySet(), messages));
  }


  public WebSocket websocket(String roomID) {
    WebSocket webSocket =  WebSocket.Text.acceptOrResult(request -> {
      if (sameOriginCheck(request)) {
        return CompletableFuture.completedFuture(F.Either.Right(flowMap.get(roomID)));
      } else {
        return CompletableFuture.completedFuture(F.Either.Left(forbidden()));
      }
    });
    return webSocket;
  }


//  public Result newConversation() {
//    DynamicForm dynamicForm = formFactory.form().bindFromRequest();
//    String roomID = dynamicForm.get("roomID");
//    flowMap.put(roomID, createUserFlowForRoom(roomID));
//    return ok(roomID);
//  }
//
  public Result newMessage() {
    DynamicForm dynamicForm = formFactory.form().bindFromRequest();
    String authorName = dynamicForm.get("authorName");
    String roomName = getRoomNameFromURL(dynamicForm.get("websocketURL"));
    String message = dynamicForm.get("message");
    String time = dynamicForm.get("time");

    String authorID = DBUtility.getUuidFromName(db, "users", authorName);
    String roomID = DBUtility.getUuidFromName(db, "chatrooms", roomName);
    DBUtility.addMessage(db, roomID, authorID, message, time);
    return ok();
  }

  /**
   * Checks that the WebSocket comes from the same origin.  This is necessary to protect
   * against Cross-Site WebSocket Hijacking as WebSocket does not implement Same Origin Policy.
   *
   * See https://tools.ietf.org/html/rfc6455#section-1.3 and
   * http://blog.dewhurstsecurity.com/2013/08/30/security-testing-html5-websockets.html
   */
  private boolean sameOriginCheck(Http.RequestHeader request) {
    String[] origins = request.headers().get("Origin");
    if (origins.length > 1) {
      // more than one origin found
      return false;
    }
    String origin = origins[0];
    return originMatches(origin);
  }

  private boolean originMatches(String origin) {
    if (origin == null) return false;
    try {
      URL url = new URL(origin);
      if (environment.isDev()) {
        return url.getHost().equals("localhost") && (url.getPort() == 9000);
      }
      else if (environment.isProd()) {
        return url.getHost().equals("chatapp-huylafa.boxfuse.io") && (url.getPort() == 9000);
      }
      else {
        return false;
      }
    } catch (Exception e ) {
      return false;
    }
  }

  private void createInitialHubs() {
    ArrayList<String> chatroomNames = DBUtility.getAllChatroomNames(db);
    for (String name : chatroomNames) {
      if (!flowMap.containsKey(name)) {
        flowMap.put(name, createUserFlowForRoom(name));
      }
    }
  }

  private Flow<String, String, NotUsed> createUserFlowForRoom(String roomName) {
    org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(this.getClass());
    LoggingAdapter logging = Logging.getLogger(actorSystem.eventStream(), logger.getName());

    //noinspection unchecked
    Source<String, Sink<String, NotUsed>> source = MergeHub.of(String.class)
            .log(roomName, logging)
            .recoverWithRetries(-1, new PFBuilder().match(Throwable.class, e -> Source.empty()).build());
    Sink<String, Source<String, NotUsed>> sink = BroadcastHub.of(String.class);

    Pair<Sink<String, NotUsed>, Source<String, NotUsed>> sinkSourcePair = source.toMat(sink, Keep.both()).run(mat);
    Sink<String, NotUsed> chatSink = sinkSourcePair.first();
    Source<String, NotUsed> chatSource = sinkSourcePair.second();
    return Flow.fromSinkAndSource(chatSink, chatSource).log(roomName, logging);
  }

  private String getRoomNameFromURL(String websocketURL) {
    int slashIndex = websocketURL.lastIndexOf('/');
    return websocketURL.substring(slashIndex + 1);
  }
}
