package controllers;

import javax.inject.Inject;

import play.mvc.Result;
import play.data.FormFactory;
import play.data.Form;
import views.html.login;
import views.formdata.UserFormData;
import views.formdata.LoginFormData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import play.db.Database;

import codeu.chat.client.Controller;
import codeu.chat.client.View;
import codeu.chat.util.RemoteAddress;
import codeu.chat.util.connections.ClientConnectionSource;
import codeu.chat.util.connections.ConnectionSource;
import codeu.chat.util.Logger;
import codeu.chat.client.ClientContext;

/**
 * Created by HuyNguyen on 4/4/17.
 */
public class LoginController extends play.mvc.Controller {

  private final static Logger.Log LOG = Logger.newLog(LoginController.class);
  private ClientContext clientContext;
  private Database db;
  @Inject FormFactory formFactory;

  @Inject
  public LoginController(Database db) { this.db = db; }

  public Result display() {
    return ok(login.render("", true));
  }

  public Result createAccount() {
    // todo. check duplicate account name
    Form<UserFormData> formData = formFactory.form(UserFormData.class).bindFromRequest();
    if (formData.hasErrors()) {
      // don't call formData.get() when there are errors, pass 'null' to helpers instead
      return badRequest(login.render("errors with log-in information", false));
    } else {
      // extract the form data
      UserFormData userForm = formData.get();
      String username = userForm.username;
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
      final Controller controller = new Controller(source);
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


  public Result login() throws SQLException {
    Form<LoginFormData> formData = formFactory.form(LoginFormData.class).bindFromRequest();
    if (formData.hasErrors()) {
      return badRequest(login.render("Username and password cannot be empty.", false));
    } else {
      // extract the form data
      LoginFormData loginForm = formData.get();
      String username = loginForm.username;
      String password = loginForm.password;

      // database query
      Connection conn = db.getConnection();
      String query = "SELECT password FROM Users WHERE Username = ?";
      PreparedStatement getPassword = conn.prepareStatement(query);
      getPassword.setString(1, username);
      ResultSet queryResult = getPassword.executeQuery();

      // if passwords match, redirect to chat page
      if (queryResult.next()) {
        String savedPassword = queryResult.getString("PASSWORD");
        if (password.equals(savedPassword)) {
          session().clear();
          session("username", username);
          return redirect(routes.ChatController.index());
        }
      }

      // if no username or wrong password, display error message
      return badRequest(login.render("Incorrect username or password", false));
    }
  }
}
