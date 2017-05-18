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

import views.html.chat;

/**
 * A chat client using WebSocket.
 */
public class ChatController extends Controller {

  private final Flow<String, String, NotUsed> userFlow, userFlow2;

  @Inject
  public ChatController(ActorSystem actorSystem,
                        Materializer mat) {
    System.out.println("injected");
    org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(this.getClass());
    LoggingAdapter logging = Logging.getLogger(actorSystem.eventStream(), logger.getName());

    //noinspection unchecked
    Source<String, Sink<String, NotUsed>> source = MergeHub.of(String.class)
            .log("source", logging)
            .recoverWithRetries(-1, new PFBuilder().match(Throwable.class, e -> Source.empty()).build());
    Sink<String, Source<String, NotUsed>> sink = BroadcastHub.of(String.class);

    Pair<Sink<String, NotUsed>, Source<String, NotUsed>> sinkSourcePair = source.toMat(sink, Keep.both()).run(mat);
    Sink<String, NotUsed> chatSink = sinkSourcePair.first();
    Source<String, NotUsed> chatSource = sinkSourcePair.second();
    this.userFlow = Flow.fromSinkAndSource(chatSink, chatSource).log("userFlow", logging);

    //noinspection unchecked
    source = MergeHub.of(String.class)
            .log("source2", logging)
            .recoverWithRetries(-1, new PFBuilder().match(Throwable.class, e -> Source.empty()).build());
    sink = BroadcastHub.of(String.class);
    sinkSourcePair = source.toMat(sink, Keep.both()).run(mat);
    chatSink = sinkSourcePair.first();
    chatSource = sinkSourcePair.second();
    this.userFlow2 = Flow.fromSinkAndSource(chatSink, chatSource).log("userFlow2", logging);
//    this.userFlow3 = Flow.fromSinkAndSource(chatSink, chatSource).log("userFlow3", logging);
  }

  public Result index() {
    if (session("username") == null) {
      return redirect(routes.LoginController.display());
    }
    Http.Request request = request();
    System.out.println("index request :" + request);
    String url = routes.ChatController.websocket("room1").webSocketURL(request);
    System.out.println("original url: " + routes.ChatController.websocket("room1").url());
    System.out.println("index url: " + url);
    return ok(chat.render(session("username"), url));
  }

  public Result index2() {
    if (session("username") == null) {
      return redirect(routes.LoginController.display());
    }
    Http.Request request = request();
    String url = routes.ChatController.chat2().webSocketURL(request);
    System.out.println("index 2 request: " + url);
    return ok(chat.render(session("username"), url));
  }

  public WebSocket websocket(String room) {
    return WebSocket.Text.acceptOrResult(request -> {
      System.out.println("give me the request: " + request);
      if (sameOriginCheck(request)) {
        return CompletableFuture.completedFuture(F.Either.Right(userFlow));
      } else {
        return CompletableFuture.completedFuture(F.Either.Left(forbidden()));
      }
    });
  }

  public WebSocket chat2() {
    return WebSocket.Text.acceptOrResult(request -> {
      if (sameOriginCheck(request)) {
        return CompletableFuture.completedFuture(F.Either.Right(userFlow2));
      } else {
        return CompletableFuture.completedFuture(F.Either.Left(forbidden()));
      }
    });
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
}
