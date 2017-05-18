package controllers;

import play.mvc.*;

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
import java.util.concurrent.CompletableFuture;
import java.util.HashMap;

import views.html.chat;

/**
 * A chat client using WebSocket.
 */
public class ChatController extends Controller {

  // maps a room ID to the user flow for that room
  private HashMap<String,Flow<String, String, NotUsed>> flowMap = new HashMap<>();
  private ActorSystem actorSystem;
  private Materializer mat;

  @Inject
  public ChatController(ActorSystem actorSystem, Materializer mat) {
    this.actorSystem = actorSystem;
    this.mat = mat;

    // create a default public room
    flowMap.put("public", createUserFlowForRoom("public"));
    flowMap.put("room1", createUserFlowForRoom("room1"));
  }


  public Result chatroom(String roomID) {
    if (session("username") == null) {
      return redirect(routes.LoginController.display());
    }
    Http.Request request = request();
    String url = routes.ChatController.websocket(roomID).webSocketURL(request);
    System.out.println("original url: " + routes.ChatController.websocket(roomID).url());
    System.out.println("index url: " + url);
    return ok(chat.render(session("username"), url));
  }

  public WebSocket websocket(String roomID) {
    return WebSocket.Text.acceptOrResult(request -> {
      System.out.println("give me the request: " + request);
      if (sameOriginCheck(request)) {
        return CompletableFuture.completedFuture(F.Either.Right(flowMap.get(roomID)));
      } else {
        return CompletableFuture.completedFuture(F.Either.Left(forbidden()));
      }
    });
  }

//
//  public Result index2() {
//    if (session("username") == null) {
//      return redirect(routes.LoginController.display());
//    }
//    Http.Request request = request();
//    String url = routes.ChatController.chat2().webSocketURL(request);
//    System.out.println("index 2 request: " + url);
//    return ok(chat.render(session("username"), url));
//  }
//
//  public WebSocket websocket(String room) {
//    return WebSocket.Text.acceptOrResult(request -> {
//      System.out.println("give me the request: " + request);
//      if (sameOriginCheck(request)) {
//        return CompletableFuture.completedFuture(F.Either.Right(userFlow));
//      } else {
//        return CompletableFuture.completedFuture(F.Either.Left(forbidden()));
//      }
//    });
//  }
//
//  public WebSocket chat2() {
//    return WebSocket.Text.acceptOrResult(request -> {
//      if (sameOriginCheck(request)) {
//        return CompletableFuture.completedFuture(F.Either.Right(userFlow2));
//      } else {
//        return CompletableFuture.completedFuture(F.Either.Left(forbidden()));
//      }
//    });
//  }

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
}
