package controllers;

import javax.inject.Singleton;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.chat;

/**
 * Created by HuyNguyen on 4/4/17.
 */
@Singleton
public class LoginController extends Controller {

  public Result display() {
    return ok(chat.render());
  }


  public Result createAccount() {
    return redirect(routes.ChatController.chat());
  }
}
