package controllers;

import javax.inject.Inject;
import javax.inject.Singleton;
import play.mvc.Controller;
import play.mvc.Result;
import play.data.Form;
import play.data.DynamicForm;
import play.api.mvc.Session;
import views.html.login;

/**
 * Created by HuyNguyen on 4/4/17.
 */
@Singleton
public class LoginController extends Controller {

  public Result display() {
    return ok(login.render(""));
  }


  public Result createAccount() {
    return ok("hello");
  }
}
