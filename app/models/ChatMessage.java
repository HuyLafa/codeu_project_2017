package models;

/**
 * Created by HuyNguyen on 5/28/17.
 */
public class ChatMessage {
  public String author;
  public String message;
  public String time;

  public ChatMessage(String author, String message, String time) {
    this.author = author;
    this.message = message;
    this.time = time;
  }
}
