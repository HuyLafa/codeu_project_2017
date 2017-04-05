package controllers;

import play.mvc.Controller;
import play.mvc.Result;

import views.html.chat;

/**
 * This controller contains an action to handle HTTP requests
 * to the application's home page.
 */
public class ChatController extends Controller {

    public Result chat() {
        return ok(chat.render());
    }

}
