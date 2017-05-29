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
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;
import java.util.HashMap;

import views.html.chat;

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

  @Inject
  public ChatController(ActorSystem actorSystem, Materializer mat, Database db) {
    this.actorSystem = actorSystem;
    this.mat = mat;
    this.db = db;

    // create two default public rooms
    String adminID = getUuidFromName("users", "admin");
    addConversation("public", adminID);
    addConversation("room1", adminID);
  }

  public Result index() {
    return redirect(routes.ChatController.chatroom("public"));
  }

  public Result chatroom(String roomID) {
    if (session("username") == null) {
      return redirect(routes.LoginController.display());
    }
    Http.Request request = request();
    String url = routes.ChatController.websocket(roomID).webSocketURL(request);
    return ok(chat.render(session("username"), url, flowMap.keySet()));
  }


  public WebSocket websocket(String roomID) {
    return WebSocket.Text.acceptOrResult(request -> {
      if (sameOriginCheck(request)) {
        return CompletableFuture.completedFuture(F.Either.Right(flowMap.get(roomID)));
      } else {
        return CompletableFuture.completedFuture(F.Either.Left(forbidden()));
      }
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

    String authorID = getUuidFromName("users", authorName);
    String roomID = getUuidFromName("chatrooms", roomName);
    addMessage(roomID, authorID, message);
    System.out.println("heloooooooooo");
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
      return url.getHost().equals("localhost")
              && (url.getPort() == 9000 || url.getPort() == 19001);
    } catch (Exception e ) {
      return false;
    }
  }

  private Flow<String, String, NotUsed> createUserFlowForRoom(String roomID) {
    org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(this.getClass());
    LoggingAdapter logging = Logging.getLogger(actorSystem.eventStream(), logger.getName());

    //noinspection unchecked
    Source<String, Sink<String, NotUsed>> source = MergeHub.of(String.class)
            .log(roomID, logging)
            .recoverWithRetries(-1, new PFBuilder().match(Throwable.class, e -> Source.empty()).build());
    Sink<String, Source<String, NotUsed>> sink = BroadcastHub.of(String.class);

    Pair<Sink<String, NotUsed>, Source<String, NotUsed>> sinkSourcePair = source.toMat(sink, Keep.both()).run(mat);
    Sink<String, NotUsed> chatSink = sinkSourcePair.first();
    Source<String, NotUsed> chatSource = sinkSourcePair.second();
    return Flow.fromSinkAndSource(chatSink, chatSource).log(roomID, logging);
  }

  private String getUuidFromName(String table, String name) {
    try {
      Connection conn = db.getConnection();
      String sqlQuery = "SELECT uuid FROM " + table + " WHERE name = ?";
      PreparedStatement getID = conn.prepareStatement(sqlQuery);
      getID.setString(1, name);
      ResultSet queryResult = getID.executeQuery();
      if (queryResult.next()) {
        String uuid = queryResult.getString("UUID");
        getID.close();
        conn.close();
        return uuid;
      } else {
        conn.close();
        return null;
      }
    } catch (SQLException e) {
      LOG.error("Error in database query: " + e);
      return null;
    }
  }


  private String getNameFromUuid(String table, String uuid) {
    try {
      Connection conn = db.getConnection();
      String sqlQuery = "SELECT name FROM " + table + " WHERE uuid = ?";
      PreparedStatement getID = conn.prepareStatement(sqlQuery);
      getID.setString(1, uuid);
      ResultSet queryResult = getID.executeQuery();
      if (queryResult.next()) {
        conn.close();
        String name = queryResult.getString("name");
        return name;
      } else {
        conn.close();
        return null;
      }
    } catch (SQLException e) {
      LOG.error("Error in database query: " + e);
      return null;
    }
  }

  private void addConversation(String title, String ownerID) {
    try {
      // add conversation to database
      Connection conn = db.getConnection();
      Conversation newConv = newConversation(title, Uuid.parse(ownerID));
      String sqlQuery = "INSERT OR IGNORE INTO chatrooms(uuid, name) VALUES (?, ?)";
      PreparedStatement insert = conn.prepareStatement(sqlQuery);
      insert.setString(1, newConv.id.toString());
      insert.setString(2, title);
      insert.executeUpdate();
      insert.close();
      conn.close();

      // add hub
      flowMap.put(title, createUserFlowForRoom(ownerID));
    } catch (IOException e) {
      LOG.error("Error creating UUID for conversation" + e);
    } catch (SQLException e) {
      LOG.error("Error adding conversation to database " + e);
    }
  }


  private Conversation newConversation(String title, Uuid owner)  {

    Conversation response = null;

    try (final codeu.chat.util.connections.Connection connection = LoginController.source.connect()) {

      // serialize the parameters
      Serializers.INTEGER.write(connection.out(), NetworkCode.NEW_CONVERSATION_REQUEST);
      Serializers.STRING.write(connection.out(), title);
      Uuid.SERIALIZER.write(connection.out(), owner);

      // send to server and deserialize response
      if (Serializers.INTEGER.read(connection.in()) == NetworkCode.NEW_CONVERSATION_RESPONSE) {
        response = Serializers.nullable(Conversation.SERIALIZER).read(connection.in());
      } else {
        LOG.error("Response from server failed.");
      }
    } catch (Exception ex) {
      System.out.println("ERROR: Exception during call on server. Check log for details.");
      LOG.error(ex, "Exception during call on server.");
    }

    return response;
  }

  private void addMessage(String chatroomID, String authorID, String message) {
    try {
      Connection conn = db.getConnection();
      String query = "INSERT INTO messages(chatroom_uuid, author_uuid, message) VALUES (?,?,?)";
      PreparedStatement statement = conn.prepareStatement(query);
      statement.setString(1, chatroomID);
      statement.setString(2, authorID);
      statement.setString(3, message);
      statement.executeUpdate();
      statement.close();
      conn.close();
    } catch (SQLException e) {
      LOG.error("error adding message to database");
    }
  }

  private String getRoomNameFromURL(String websocketURL) {
    int slashIndex = websocketURL.lastIndexOf('/');
    return websocketURL.substring(slashIndex + 1);
  }
}
