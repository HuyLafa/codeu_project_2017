package models;

import codeu.chat.common.NetworkCode;
import codeu.chat.common.User;
import codeu.chat.util.Logger;
import codeu.chat.util.RemoteAddress;
import codeu.chat.util.Serializers;
import codeu.chat.util.connections.ClientConnectionSource;
import codeu.chat.util.connections.ConnectionSource;
import codeu.chat.util.connections.Connection;

import java.io.IOException;

/**
 * Created by HuyNguyen on 5/29/17.
 */
public class Models {

  // the logger to output error messages
  private static final Logger.Log LOG = Logger.newLog(Models.class);

  // the source that connects to the server.
  public static ConnectionSource source = establishSource();

  // the default remote address
  static final String REMOTE_ADDRESS = "localhost@2007";

  /**
   * Establish a connection to a server at the default remote address.
   * @return a <tt>ClientConnectionSource</tt> instance.
   */
  public static ClientConnectionSource establishSource() {
    try {
      RemoteAddress address = RemoteAddress.parse(REMOTE_ADDRESS);
      return new ClientConnectionSource(address.host, address.port);
    } catch (Exception ex) {
      System.out.println("ERROR: Exception setting up client. Check log for details.");
      LOG.error(ex, "Exception setting up client.");
    }
    return null;
  }

  /**
   * Create a new user. This is the same method in codeu.chat.server.Controller
   * @param name the username.
   * @return an instance of <tt>User</tt> containing the input information.
   */
  public static User newUser(Connection connection, String name) throws IOException {

    User response = null;

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

    return response;
  }

  public static User newUser(String name) {
    User response = null;
    try {
      response = newUser(source.connect(), name);
    } catch (Exception ex) {
      System.out.println("ERROR: Exception during call on server. Check log for details.");
      LOG.error(ex, "Exception during call on server.");
    }
    return response;
  }
}
