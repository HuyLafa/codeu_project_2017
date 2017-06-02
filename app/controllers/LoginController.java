package controllers;

import javax.inject.Inject;
import javax.inject.Singleton;

import codeu.chat.common.User;
import models.Models;
import models.UserForm;

import play.data.Form;
import play.mvc.Result;
import play.mvc.Controller;
import play.data.FormFactory;
import play.data.DynamicForm;
import play.db.Database;
import views.html.login;

import models.DBUtility;

/**
 * Created by HuyNguyen on 4/4/17.
 */
@Singleton
public class LoginController extends Controller {

  // injected database instance
  private Database db;

  // a tool to extract parameter values from HTTP requests
  @Inject FormFactory formFactory;


  /**
   * Constructor used for dependency injection.
   */
  @Inject
  public LoginController(Database inputDB) {
    db = inputDB;
  }

  /**
   * Display the default login page.
   * @return the login page.
   */
  public Result display() {
    return ok(login.render());
  }

  /**
   * Accepts a request to create account and call the corresponding backend database method.
   * @return the login page with the output message (success / error)
   */
  public Result createAccount() {
    Form<UserForm> formData = formFactory.form(UserForm.class).bindFromRequest();
    if (formData.hasErrors()) {
      // don't call formData.get() when there are errors, pass 'null' to helpers instead
      flash("error", "Error: Username must be nonempty and password must be at least 6 characters long.");
      return badRequest(login.render());
    } else {
      // extract the form data
      UserForm user = formData.bindFromRequest().get();
      String username = user.getUsername();
      String password = user.getPassword();

      // check duplicate
      if (DBUtility.checkDuplicateField(db, "users", "name", username)) {
        flash("error", "Username is already taken.");
        return badRequest(login.render());
      }

      // add user to the database
      User newUser = Models.newUser(username);
      DBUtility.addUser(db, username, password, newUser.id.toString());
      flash("success", "Create account successfully. Please log in.");
      return redirect(routes.LoginController.display());

    }
  }

  /**
   * Initialize the controller and the view based on connection source, then redirect to login page.
   */
  public Result index() {
      // if user already logged in
      if (session("username") != null) {
        return redirect(routes.ChatController.index());
      }
      // else lead to log-in page
      return redirect(routes.LoginController.display());
  }

  /**
   * Accept a request to log in from the user, and call the database methods to check login credentials.
   * @return the login page with error message if there's an error, or the chat page is login is successful.
   */
  public Result login() {
    DynamicForm formData = formFactory.form().bindFromRequest();
    if (formData.hasErrors()) {
      flash("error", "Username and password cannot be empty.");
      return badRequest(login.render());
    } else {
      // extract the form data
      String username = formData.get("username");
      String password = formData.get("password");

      String dbPassword = DBUtility.getPasswordFromUsername(db, username);

      // if no username or wrong password, display error message
      if (dbPassword == null || !dbPassword.equals(password)) {
        flash("error", "Incorrect username or password");
        return badRequest(login.render());
      }

      // if passwords match, redirect to chat page
      session().clear();
      session("username", username);
      return redirect(routes.ChatController.index());
    }
  }

  /**
   * Log the user out by clearing the session.
   * @return the login page.
   */
  public Result logout() {
    session().clear();
    flash("success", "Log out successfully.");
    return redirect(routes.LoginController.display());
  }
}
