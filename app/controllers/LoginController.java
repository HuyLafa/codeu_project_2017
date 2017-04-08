package controllers;

import javax.inject.Inject;
import javax.inject.Singleton;

import play.mvc.Controller;
import play.mvc.Result;
import play.data.FormFactory;
import play.data.Form;
import views.html.login;
import views.formdata.UserFormData;

import codeu.chat.client.View;
import codeu.chat.util.RemoteAddress;
import codeu.chat.util.connections.ClientConnectionSource;
import codeu.chat.util.connections.ConnectionSource;
import codeu.chat.util.Logger;
import codeu.chat.client.BackendController;
import codeu.chat.client.ClientContext;

/**
 * Created by HuyNguyen on 4/4/17.
 */
@Singleton
public class LoginController extends Controller {

  private final static Logger.Log LOG = Logger.newLog(LoginController.class);
  private ClientContext clientContext;
  @Inject FormFactory formFactory;

  public Result display() {
    return ok(login.render("", true));
  }

  public Result createAccount() {
    Form<UserFormData> formData = formFactory.form(UserFormData.class).bindFromRequest();
    if (formData.hasErrors()) {
      // don't call formData.get() when the are errors, pass 'null' to helpers instead
      System.out.println(formData.error("name"));
      System.out.println(formData.error("password"));
      return badRequest(login.render("errors with log-in information", false));
    } else {
      // extract the form data
      UserFormData userForm = formData.get();
      String username = userForm.name;
      String password = userForm.password;
      clientContext.user.addUser(username);
      return ok(login.render("Create account successful. Please log in.", true));
    }
  }

  /**
   * Initialize the controller and the view based on connection source,
   * then redirect to login page.
   */
  public Result index() {
    final RemoteAddress address = RemoteAddress.parse("localhost@2007");

    try (final ConnectionSource source = new ClientConnectionSource(address.host, address.port)) {
      // initialize the controller and view based on connection source
      final BackendController controller = new BackendController(source);
      final View view = new View(source);
      clientContext = new ClientContext(controller, view);
      LOG.info("Creating client...");
      return redirect(routes.LoginController.display());

    } catch (Exception ex) {
      System.out.println("ERROR: Exception setting up client. Check log for details.");
      LOG.error(ex, "Exception setting up client.");
      return badRequest(login.render("Exception setting up client", false));
    }
  }
}
