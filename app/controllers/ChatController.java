package controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

import models.ChatMessage;
import models.DBUtility;

import views.html.chat;
import play.libs.F;

/**
 * A chat client using WebSocket.
 */
@Singleton
public class ChatController extends Controller {

  // maps a room ID to the user flow for that room
  private HashMap<String,Flow<String, String, NotUsed>> flowMap = new HashMap<>();
  private ActorSystem actorSystem;
  private Materializer mat;
  private Database db;
  @Inject FormFactory formFactory;

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


  public WebSocket websocket(String roomName) {
    return WebSocket.Text.acceptOrResult(request -> {
      return CompletableFuture.completedFuture(F.Either.Right(flowMap.get(roomName)));
    });
    

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
