package controllers;

import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClient;
import org.asynchttpclient.ws.WebSocket;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import play.libs.ws.WS;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;
import play.test.WithServer;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertEquals;
import static play.mvc.Http.Status.OK;

/**
 * Test WebSockets in ChatController
 */
public class ChatControllerTest extends WithServer {

  private AsyncHttpClient asyncHttpClient;

  @Before
  public void setUp() {
    asyncHttpClient = new DefaultAsyncHttpClient();
  }

  @After
  public void tearDown() throws IOException {
    asyncHttpClient.close();
  }

  @Test
  public void testInServer() throws Exception {
    String url = "http://localhost:" + this.testServer.port() + "/";
    try (WSClient ws = WS.newClient(this.testServer.port())) {
      CompletionStage<WSResponse> stage = ws.url(url).get();
      WSResponse response = stage.toCompletableFuture().get();
      assertEquals(303, response.getStatus());
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

}