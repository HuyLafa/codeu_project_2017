package models;

/**
 * A container for a chat message (unused).
 */
public class ChatMessage {
  public String author;
  public String message;
  public String time;

  /**
   * The class constructor, which takes and saves input information.
   * @param author the name of the message author.
   * @param message the message content.
   * @param time the time the message was sent.
   */
  public ChatMessage(String author, String message, String time) {
    this.author = author;
    this.message = message;
    this.time = time;
  }
}
