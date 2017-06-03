package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import play.Mode;
import play.Application;
import play.inject.guice.GuiceApplicationBuilder;
import play.test.FakeApplication;
import play.test.Helpers;
import play.mvc.Http.RequestBuilder;

import static org.junit.Assert.assertEquals;
import static play.test.Helpers.route;

/**
 * Test methods in LoginController
 */
public class LoginControllerTest {

  protected Application application;

  @Before
  public void startApp() throws Exception {
    ClassLoader classLoader = FakeApplication.class.getClassLoader();
    application = new GuiceApplicationBuilder().in(classLoader)
            .in(Mode.TEST).build();
    Helpers.start(application);
  }

  @Test
  public void testLogin() throws Exception {

    // successful login should redirect to /chat
    JsonNode jsonNode = (new ObjectMapper()).readTree("{ \"username\": \"admin\", \"password\": \"123456\" }");
    RequestBuilder request = new RequestBuilder().method("POST")
            .bodyJson(jsonNode)
            .uri(controllers.routes.LoginController.login().url());
    assertEquals(303, route(request).status());

    // unsuccessful login should return 400
    jsonNode = (new ObjectMapper()).readTree("{ \"username\": \"randomuser\", \"password\": \"123456\" }");
    request.bodyJson(jsonNode);
    assertEquals(400, route(request).status());

  }


  @Test
  public void testCreateAccount() throws Exception {

    // successful creating account should redirect to /chat
    String randomUsername = System.currentTimeMillis() + "";
    String params = String.format("{ \"username\": \"%s\", \"password\": \"abcdef\" }", randomUsername);
    // successful create account should redirect to /login
    JsonNode jsonNode = (new ObjectMapper()).readTree(params);
    RequestBuilder request = new RequestBuilder().method("POST")
            .bodyJson(jsonNode)
            .uri(controllers.routes.LoginController.createAccount().url());

    assertEquals(303, route(request).status());

    // error (duplicate username) should return 400
    assertEquals(400, route(request).status());

    // error (empty username) should return 400
    params = String.format("{ \"username\": \"%s\", \"password\": \"abcdef\" }", "");
    jsonNode = (new ObjectMapper()).readTree(params);
    request.bodyJson(jsonNode);
    assertEquals(400, route(request).status());

    // error (password length < 6) should return 400
    randomUsername = System.currentTimeMillis() + "";
    params = String.format("{ \"username\": \"%s\", \"password\": \"%s\" }", randomUsername, "abc");
    jsonNode = (new ObjectMapper()).readTree(params);
    request.bodyJson(jsonNode);
    assertEquals(400, route(request).status());
  }

  @After
  public void stopApp() throws Exception {
    Helpers.stop(application);
  }

}
