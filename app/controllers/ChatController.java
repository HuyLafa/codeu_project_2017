package controllers;

import codeu.chat.util.Uuid;
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

  // injected materials for creating user flow
  private ActorSystem actorSystem;
  private Materializer mat;

  // injected database instance
  private Database db;

  // a tool to extract parameter values from HTTP requests
  @Inject FormFactory formFactory;

  // the default chatroom that the user enters after logging in
  final String DEFAULT_CHATROOM = "public";

  /**
   * Constructor used for dependency injection.
   */
  @Inject
  public ChatController(ActorSystem actorSystem, Materializer mat, Database db) {
    this.actorSystem = actorSystem;
    this.mat = mat;
    this.db = db;
    createInitialHubs();
  }

  /**
   * The default Action after the user logs in
   * @return a redirect to the default chatroom.
   */
  public Result index() {
    return redirect(routes.ChatController.chatroom(DEFAULT_CHATROOM));
  }

  /**
   * Get the chatroom with the specified name, or redirect to login page if user is not logged in
   * @param roomName the input room name.
   * @return the chatroom page with the corresponding websocket.
   */
  public Result chatroom(String roomName) {
    if (session("username") == null) {
      return redirect(routes.LoginController.display());
    }

    // get the websocket URL
    Http.Request request = request();
    String url = routes.ChatController.websocket(roomName).webSocketURL(request);
    ArrayList<String> permittedRooms = DBUtility.getChatroomNamesForUser(db, session("username"));
    return ok(chat.render(session("username"), url, permittedRooms));
  }

  /**
   * Get a WebSocket for the specified room.
   * @param roomName the input room name.
   * @return a WebSocket with the corresponding user flow.
   */
  public WebSocket websocket(String roomName) {
    return WebSocket.Text.acceptOrResult(request -> {
      return CompletableFuture.completedFuture(F.Either.Right(flowMap.get(roomName)));
    });
  }

  /**
   * Add a new chat room.
   * @return the name of the added chat room.
   */
  public Result newConversation() {
    // extract parameters
    DynamicForm dynamicForm = formFactory.form().bindFromRequest();
    String roomName = dynamicForm.get("roomName");
    String owner = session("username");

    // add chatroom to database
    DBUtility.addConversation(db, roomName, owner);
    flowMap.put(roomName, createUserFlowForRoom(roomName));
    return ok(roomName);
  }
  /**
  * Add a new user to a chat room
  * @return the name of the user
  */

  public Result addUserToRoom() {
    // extract parameters
    DynamicForm dynamicForm = formFactory.form().bindFromRequest();
    String roomname = dynamicForm.get("roomname");
    String user = dynamicForm.get("user");

    DBUtility.addUserToRoom(db, roomname, user);
    return ok();

  }

  /**
   * Add a new message.
   * @return an OK status.
   */
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
   * Create a user flow for each chat room when the user first logs in.
   */
  private void createInitialHubs() {
    ArrayList<String> chatroomNames = DBUtility.getAllChatroomNames(db);
    for (String name : chatroomNames) {
      if (!flowMap.containsKey(name)) {
        flowMap.put(name, createUserFlowForRoom(name));
      }
    }
  }

  /**
   * Create a user flow for the specified room name.
   * @param roomName the input room name.
   * @return a flow created from a pair of Sink and Source from the injected materials.
   */
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

  /**
   * Extract the chatroom's name from the WebSocket URL by taking the last part after "/"
   * @param websocketURL the input WebSocket URL
   * @return the chatroom's name.
   */
  private String getRoomNameFromURL(String websocketURL) {
    int slashIndex = websocketURL.lastIndexOf('/');
    return websocketURL.substring(slashIndex + 1);
  }
}
