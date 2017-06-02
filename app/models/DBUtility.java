package models;

import codeu.chat.common.User;
import codeu.chat.util.Logger;
import play.db.Database;

import java.sql.*;
import java.util.ArrayList;

/**
 * Created by HuyNguyen on 5/29/17.
 */
public class DBUtility {

  // the logger used to printing out error messages
  private static final Logger.Log LOG = Logger.newLog(DBUtility.class);

  /**
   * Check if a tuple with the specified value of a field already exists in the database.
   * @param conn the database connection, closed at the end of this method.
   * @param tableName the name of the table to check for.
   * @param fieldName the column name.
   * @param value the value to check for duplicate.
   * @return <tt>true</tt> if there is a duplicate and <tt>false</tt> otherwise.
   */
  public static boolean checkDuplicateField(Connection conn, String tableName, String fieldName, String value) {
    try {
      String findQuery = String.format("SELECT * FROM %s WHERE %s = ?", tableName, fieldName);
      PreparedStatement getRows = conn.prepareStatement(findQuery);
      getRows.setString(1, value);
      ResultSet queryResult = getRows.executeQuery();
      boolean exist = queryResult.next();
      getRows.close();
      conn.close();
      return exist;
    } catch (SQLException e) {
      LOG.error("Error looking up database");
      return false;
    }
  }

  /**
   * Check if a tuple with the specified value of a field already exists in the database.
   * @param db the database instance.
   * @param tableName the name of the table to check for.
   * @param fieldName the column name.
   * @param value the value to check for duplicate.
   * @return <tt>true</tt> if there is a duplicate and <tt>false</tt> otherwise.
   */
  public static boolean checkDuplicateField(Database db, String tableName, String fieldName, String value) {
    return checkDuplicateField(db.getConnection(), tableName, fieldName, value);
  }

  /**
   * Get the saved password in the database from the input username.
   * @param conn the database connection.
   * @param username the input username.
   * @return the saved password or <tt>null</tt> if username does not exist.
   */
  public static String getPasswordFromUsername(Connection conn, String username) {
    try {
      String query = "SELECT password FROM Users WHERE name = ?";
      PreparedStatement getPassword = conn.prepareStatement(query);
      getPassword.setString(1, username);
      ResultSet queryResult = getPassword.executeQuery();
      if (queryResult.next()) {
        String password = queryResult.getString("password");
        conn.close();
        return password;
      }
    } catch (SQLException e) {
      LOG.error("error looking up password");
    }
    return null;
  }


  /**
   * Get the saved password in the database from the input username.
   * @param db the database instance.
   * @param username the input username.
   * @return the saved password or <tt>null</tt> if username does not exist.
   */
  public static String getPasswordFromUsername(Database db, String username) {
    return getPasswordFromUsername(db.getConnection(), username);
  }

  /**
   * Get the uuid saved in the database from the input name.
   * @param conn the database connection.
   * @param table the table to look for.
   * @param name the input name.
   * @return the uuid string or <tt>null</tt> if <tt>name</tt> does not exist.
   */
  public static String getUuidFromName(Connection conn, String table, String name) {
    try {
      String sqlQuery = "SELECT uuid FROM " + table + " WHERE name = ?";
      PreparedStatement getID = conn.prepareStatement(sqlQuery);
      getID.setString(1, name);
      ResultSet queryResult = getID.executeQuery();
      if (queryResult.next()) {
        String uuid = queryResult.getString("UUID");
        getID.close();
        conn.close();
        return uuid;
      } else {
        conn.close();
        return null;
      }
    } catch (SQLException e) {
      LOG.error("Error in database query: " + e);
      return null;
    }
  }

  /**
   * Get the uuid saved in the database from the input name.
   * @param db the database instance.
   * @param table the table to look for.
   * @param name the input name.
   * @return the uuid string or <tt>null</tt> if <tt>name</tt> does not exist.
   */
  public static String getUuidFromName(Database db, String table, String name) {
    return getUuidFromName(db.getConnection(), table, name);
  }


  /**
   * Get the name saved in the database from the input uuid string.
   * @param conn the database connection.
   * @param table the table to look for.
   * @param uuid the input uuid.
   * @return the saved name or <tt>null</tt> if <tt>uuid</tt> does not exist.
   */
  public static String getNameFromUuid(Connection conn, String table, String uuid) {
    try {
      String sqlQuery = "SELECT name FROM " + table + " WHERE uuid = ?";
      PreparedStatement getID = conn.prepareStatement(sqlQuery);
      getID.setString(1, uuid);
      ResultSet queryResult = getID.executeQuery();
      if (queryResult.next()) {
        String name = queryResult.getString("name");
        queryResult.close();
        conn.close();
        return name;
      } else {
        conn.close();
        return null;
      }
    } catch (SQLException e) {
      LOG.error("Error in database query: " + e);
      return null;
    }
  }

  /**
   * Get the name saved in the database from the input uuid string.
   * @param db the database instance.
   * @param table the table to look for.
   * @param uuid the input uuid.
   * @return the saved name or <tt>null</tt> if <tt>uuid</tt> does not exist.
   */
  public static String getNameFromUuid(Database db, String table, String uuid) {
    return getNameFromUuid(db.getConnection(), table, uuid);
  }

  /**
   * Get all the messages saved in the database from a chatroom (unused).
   * @param db the database instance.
   * @param chatroomName the name of the chatroom.
   * @return a list of messages in chronological order.
   */
  public static ArrayList<ChatMessage> getAllMessages(Database db, String chatroomName) {
    ArrayList<ChatMessage> result = new ArrayList<>();
    try {
      String chatroomID = getUuidFromName(db, "chatrooms", chatroomName);
      String query = "SELECT * FROM messages WHERE chatroom_uuid = ?";
      Connection conn = db.getConnection();
      PreparedStatement statement = conn.prepareStatement(query);
      statement.setString(1, chatroomID);
      ResultSet messages =  statement.executeQuery();
      while (messages.next()) {
        String authorID = messages.getString("author_uuid");
        String author = getNameFromUuid(db, "users", authorID);
        String message = messages.getString("message");
        String time = messages.getString("time");
        ChatMessage chatMessage = new ChatMessage(author, message, time);
        result.add(chatMessage);
      }
      messages.close();
      conn.close();
    } catch (SQLException e) {
      LOG.error("error retrieving messages from chat room " + chatroomName);
    } finally {
      return result;
    }
  }

  /**
   * Get the name of all chatrooms saved in the database.
   * @param conn the databse connection.
   * @return an <tt>ArrayList</tt> of all chatroom names.
   */
  public static ArrayList<String> getAllChatroomNames(Connection conn) {
    ArrayList<String> result = new ArrayList<>();
    try {
      PreparedStatement statement = conn.prepareStatement("SELECT name FROM chatrooms");
      ResultSet names = statement.executeQuery();
      while (names.next()) {
        result.add(names.getString("name"));
      }
      conn.close();
    } catch (SQLException e) {
      LOG.error("error retrieving chatroom names");
    } finally {
      return result;
    }
  }

  /**
   * Get the name of all chatrooms saved in the database.
   * @param db the database connection.
   * @return an <tt>ArrayList</tt> of all chatroom names.
   */
  public static ArrayList<String> getAllChatroomNames(Database db) {
    return getAllChatroomNames(db.getConnection());
  }

  /**
   * Add a message to the database (unused).
   * @param db the database instance.
   * @param chatroomID the uuid of the chatroom.
   * @param authorID the uuid of the author.
   * @param message the message content.
   * @param time the time the message was sent.
   */
  public static void addMessage(Database db, String chatroomID, String authorID, String message, String time) {
    try {
      Connection conn = db.getConnection();
      String query = "INSERT INTO messages(chatroom_uuid, author_uuid, message, time) VALUES (?,?,?,?)";
      PreparedStatement statement = conn.prepareStatement(query);
      statement.setString(1, chatroomID);
      statement.setString(2, authorID);
      statement.setString(3, message);
      statement.setString(4, time);
      statement.executeUpdate();
      statement.close();
      conn.close();
    } catch (SQLException e) {
      LOG.error("error adding message to database");
    }
  }

  /**
   * Add a new user to the database.
   * @param conn the database connection.
   * @param username the input username.
   * @param password the input password.
   * @return an instance of <tt>User</tt>.
   */
  public static User addUser(Connection conn, String username, String password) {
    try {
      User user = Models.newUser(username);
      String insertQuery = "INSERT OR IGNORE INTO users(uuid, name, password) VALUES(?, ?, ?)";
      PreparedStatement insert = conn.prepareStatement(insertQuery);
      insert.setString(1, user.id.toString());
      insert.setString(2, username);
      insert.setString(3, password);
      insert.executeUpdate();
      insert.close();
      conn.close();
      return user;
    } catch (SQLException e) {
      LOG.error("error adding user to database");
      return null;
    }
  }

  /**
   * Add a new user to the database.
   * @param db the database instance.
   * @param username the input username.
   * @param password the input password.
   * @return an instance of <tt>User</tt>.
   */
  public static User addUser(Database db, String username, String password) {
    return addUser(db.getConnection(), username, password);
  }

  /**
   * Add a new chatroom to the database.
   * @param conn the databse connection.
   * @param title the name of the chatroom.
   */
  public static void addConversation(Connection conn, String title) {
    try {
      String sqlQuery = "INSERT OR IGNORE INTO chatrooms(name) VALUES (?)";
      PreparedStatement insert = conn.prepareStatement(sqlQuery);
      insert.setString(1, title);
      insert.executeUpdate();
      insert.close();
      conn.close();
    } catch (SQLException e) {
      LOG.error("Error adding conversation to database " + e);
    }
  }

  /**
   * Add a new chatroom to the database.
   * @param db the database instance.
   * @param title the name of the chatroom.
   */
  public static void addConversation(Database db, String title) {
    addConversation(db.getConnection(), title);
  }

}
