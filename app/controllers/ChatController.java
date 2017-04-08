package controllers;

import play.mvc.Controller;
import play.mvc.Result;
import javax.inject.Singleton;

import views.html.chat;

@Singleton
public class ChatController extends Controller {

  public Result index() {
    return ok(chat.render(session("username")));
  }

}
