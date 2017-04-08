package controllers;

import javax.inject.Singleton;

import codeu.chat.client.BackendController;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.login;

import codeu.chat.client.View;
import codeu.chat.util.RemoteAddress;
import codeu.chat.util.connections.ClientConnectionSource;
import codeu.chat.util.connections.ConnectionSource;
import codeu.chat.util.Logger;

/**
 * Created by HuyNguyen on 4/4/17.
 */
@Singleton
public class LoginController extends Controller {

  private final static Logger.Log LOG = Logger.newLog(LoginController.class);

  public Result display() {
    return ok(login.render(""));
  }


  public Result createAccount() {
    return ok("hello");
  }


  public Result index() {
    final RemoteAddress address = RemoteAddress.parse("localhost@2007");

    try (final ConnectionSource source = new ClientConnectionSource(address.host, address.port)) {
      final BackendController controller = new BackendController(source);
      final View view = new View(source);

      LOG.info("Creating client...");

//      runClient(controller, view);

    } catch (Exception ex) {
      System.out.println("ERROR: Exception setting up client. Check log for details.");
      LOG.error(ex, "Exception setting up client.");
    }
  }
}
