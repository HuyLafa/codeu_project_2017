package controllers;

import javax.inject.Inject;

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
public class LoginController extends Controller {

  private Database db;
  @Inject FormFactory formFactory;

  @Inject
  public LoginController(Database inputDB) {
    db = inputDB;
  }

  public Result display() {
    return ok(login.render());
  }

  public Result createAccount() {
    DynamicForm formData = formFactory.form().bindFromRequest();
    if (formData.hasErrors()) {
      // don't call formData.get() when there are errors, pass 'null' to helpers instead
      flash("error", "Errors with log-in information");
      return badRequest(login.render());
    } else {
      // extract the form data
      String username = formData.get("username");
      String password = formData.get("password");

      // check duplicate
      if (DBUtility.checkDuplicateField(db, "users", "name", username)) {
        flash("error", "Username is already taken.");
        return badRequest(login.render());
      }

      // add user to the database
      DBUtility.addUser(db, username, password);
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

  public Result logout() {
    session().clear();
    flash("success", "Log out successfully.");
    return redirect(routes.LoginController.display());
  }
}
