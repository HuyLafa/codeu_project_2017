package models;

import codeu.chat.common.Conversation;
import codeu.chat.common.NetworkCode;
import codeu.chat.common.User;
import codeu.chat.util.Logger;
import codeu.chat.util.RemoteAddress;
import codeu.chat.util.Serializers;
import codeu.chat.util.Uuid;
import codeu.chat.util.connections.ClientConnectionSource;
import codeu.chat.util.connections.ConnectionSource;
import controllers.LoginController;

/**
 * Created by HuyNguyen on 5/29/17.
 */
public class Models {

  private static final Logger.Log LOG = Logger.newLog(Models.class);
  public static final ConnectionSource source = establishSource();

  private static ClientConnectionSource establishSource() {
    try {
      RemoteAddress address = RemoteAddress.parse("localhost@2007");
      return new ClientConnectionSource(address.host, address.port);
    } catch (Exception ex) {
      System.out.println("ERROR: Exception setting up client. Check log for details.");
      LOG.error(ex, "Exception setting up client.");
    }
    return null;
  }

  public static Conversation newConversation(String title, Uuid owner)  {

    Conversation response = null;

    try (final codeu.chat.util.connections.Connection connection = source.connect()) {
      // serialize the parameters
      Serializers.INTEGER.write(connection.out(), NetworkCode.NEW_CONVERSATION_REQUEST);
      Serializers.STRING.write(connection.out(), title);
      Uuid.SERIALIZER.write(connection.out(), owner);

      // send to server and deserialize response
      if (Serializers.INTEGER.read(connection.in()) == NetworkCode.NEW_CONVERSATION_RESPONSE) {
        response = Serializers.nullable(Conversation.SERIALIZER).read(connection.in());
      } else {
        System.out.println("Response from server failed.");
      }
    } catch (Exception ex) {
      System.out.println("ERROR: Exception during call on server. Check log for details.");
      LOG.error(ex, "Exception during call on server.");
    }
    System.out.println("return response" + response);
    return response;
  }

  public static User newUser(String name) {

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
}
