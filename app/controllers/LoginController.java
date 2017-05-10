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

  private Database db;
  @Inject FormFactory formFactory;

  @Inject
  public LoginController(Database db) { this.db = db; }

  public Result display() {
    return ok(login.render());
  }

  public Result createAccount() throws SQLException {
    Form<UserFormData> formData = formFactory.form(UserFormData.class).bindFromRequest();
    if (formData.hasErrors()) {
      // don't call formData.get() when there are errors, pass 'null' to helpers instead
      flash("error", "Errors with log-in information");
      return badRequest(login.render());
    } else {
      // extract the form data
      UserFormData userForm = formData.get();
      String username = userForm.username;
      String password = userForm.password;

      // database query
      Connection conn = db.getConnection();
      String findQuery = "SELECT * FROM Users where Username = ?";
      PreparedStatement getUser = conn.prepareStatement(findQuery);
      getUser.setString(1, username);
      ResultSet queryResult = getUser.executeQuery();
      getUser.close();

      if (queryResult.next()) {
        flash("error", "Username is already taken.");
        return badRequest(login.render());
      }

      // add user to the database
      String addQuery = "INSERT INTO Users(username, password) VALUES (?, ?)";
      PreparedStatement addUser = conn.prepareStatement(addQuery);
      addUser.setString(1, username);
      addUser.setString(2, password);
      addUser.executeUpdate();
      addUser.close();

      conn.close();
      flash("success", "Create account successfully. Please log in.");
      return redirect(routes.LoginController.display());
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

      // if user already logged in
      if (session("username") != null) {
        return redirect(routes.ChatController.index());
      }
      // else lead to log-in page
      return redirect(routes.LoginController.display());

    } catch (Exception ex) {
      System.out.println("ERROR: Exception setting up client. Check log for details.");
      flash("error", "Exception setting up client.");
      return badRequest(login.render());
    }
  }


  public Result login() throws SQLException {
    Form<LoginFormData> formData = formFactory.form(LoginFormData.class).bindFromRequest();
    if (formData.hasErrors()) {
      flash("error", "Username and password cannot be empty.");
      return badRequest(login.render());
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
          getPassword.close();
          conn.close();
          session("username", username);
          return redirect(routes.ChatController.index());
        }
      }

      getPassword.close();
      conn.close();
      // if no username or wrong password, display error message
      flash("error", "Incorrect username or password");
      return badRequest(login.render());
    }
  }

  public Result logout() {
    session().clear();
    flash("success", "Log out successfully.");
    return redirect(routes.LoginController.display());
  }
}
