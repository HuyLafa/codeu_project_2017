package controllers;

import javax.inject.Inject;

import play.mvc.Result;
import play.mvc.Controller;
import play.data.FormFactory;
import play.data.DynamicForm;
import play.db.Database;
import views.html.login;

import java.rmi.Remote;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import codeu.chat.util.RemoteAddress;
import codeu.chat.util.connections.ClientConnectionSource;
import codeu.chat.util.connections.ConnectionSource;
import codeu.chat.util.Logger;
import codeu.chat.common.NetworkCode;
import codeu.chat.common.User;
import codeu.chat.util.Serializers;

/**
 * Created by HuyNguyen on 4/4/17.
 */
public class LoginController extends Controller {

  public static final ConnectionSource source = establishSource();
  private static final Logger.Log LOG = Logger.newLog(LoginController.class);
  private Database db;
  @Inject FormFactory formFactory;

  @Inject
  public LoginController(Database inputDB) {
    db = inputDB;

    // create a default admin account
    try {
      User admin = newUser("admin");
      Connection conn = db.getConnection();
      String insertQuery = "INSERT OR IGNORE INTO users(uuid, username, password) VALUES(?, ?, ?)";
      PreparedStatement insertAdmin = conn.prepareStatement(insertQuery);
      insertAdmin.setString(1, admin.id.toString());
      insertAdmin.setString(2, "admin");
      insertAdmin.setString(3, "123456");
      insertAdmin.executeUpdate();
      insertAdmin.close();
      conn.close();
    } catch (SQLException e) {
      LOG.error("Error adding admin to database");
    }
  }

  public Result display() {
    return ok(login.render());
  }

  public Result createAccount() throws SQLException {
    DynamicForm formData = formFactory.form().bindFromRequest();
    if (formData.hasErrors()) {

      // don't call formData.get() when there are errors, pass 'null' to helpers instead
      flash("error", "Errors with log-in information");
      return badRequest(login.render());

    } else {

      // extract the form data
      String username = formData.get("username");
      String password = formData.get("password");

      // database query
      Connection conn = db.getConnection();
      String findQuery = "SELECT * FROM Users where Username = ?";
      PreparedStatement getUser = conn.prepareStatement(findQuery);
      getUser.setString(1, username);
      ResultSet queryResult = getUser.executeQuery();

      if (queryResult.next()) {
        getUser.close();
        conn.close();
        flash("error", "Username is already taken.");
        return badRequest(login.render());
      }

      getUser.close();

      // send new user request to server
      User newUser = newUser(username);

      // add user to the database
      String addQuery = "INSERT INTO Users(username, password, uuid) VALUES (?, ?, ?)";
      PreparedStatement addUser = conn.prepareStatement(addQuery);
      addUser.setString(1, username);
      addUser.setString(2, password);
      addUser.setString(3, newUser.id.toString());
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
      // if user already logged in
      if (session("username") != null) {
        return redirect(routes.ChatController.index());
      }
      // else lead to log-in page
      return redirect(routes.LoginController.display());
  }

  public Result login() throws SQLException {
    DynamicForm formData = formFactory.form().bindFromRequest();
    if (formData.hasErrors()) {
      flash("error", "Username and password cannot be empty.");
      return badRequest(login.render());
    } else {
      // extract the form data
      String username = formData.get("username");
      String password = formData.get("password");

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

  public User newUser(String name) {

    User response = null;

    try (final codeu.chat.util.connections.Connection connection = source.connect()) {

      // serialize the parameters
      Serializers.INTEGER.write(connection.out(), NetworkCode.NEW_USER_REQUEST);
      Serializers.STRING.write(connection.out(), name);
      LOG.info("newUser: Request completed.");

      // send to server and deserialize response
      if (Serializers.INTEGER.read(connection.in()) == NetworkCode.NEW_USER_RESPONSE) {
        response = Serializers.nullable(User.SERIALIZER).read(connection.in());
        LOG.info("newUser: Response completed.");
      } else {
        LOG.error("Response from server failed.");
      }
    } catch (Exception ex) {
      System.out.println("ERROR: Exception during call on server. Check log for details.");
      LOG.error(ex, "Exception during call on server.");
    }

    return response;
  }

  private static ClientConnectionSource establishSource() {
    try {
      if (source == null) {
        RemoteAddress address = RemoteAddress.parse("localhost@2007");
        return new ClientConnectionSource(address.host, address.port);
      }

    } catch (Exception ex) {
      System.out.println("ERROR: Exception setting up client. Check log for details.");
      LOG.error(ex, "Exception setting up client.");
      flash("error", "Exception setting up client.");
    }
    return null;
  }
}
