package controllers;

import play.mvc.Controller;
import play.mvc.Result;
import javax.inject.Singleton;

import views.html.chat;

public class ChatController extends Controller {

  public Result index() {
    if (session("username") == null) {
      return redirect(routes.LoginController.display());
    }
    return ok(chat.render(session("username")));
  }

}
