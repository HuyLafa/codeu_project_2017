package models;

import codeu.chat.common.Conversation;
import codeu.chat.common.User;
import codeu.chat.util.Logger;
import codeu.chat.util.Uuid;
import play.db.Database;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;

/**
 * Created by HuyNguyen on 5/29/17.
 */
public class DBUtility {

  private static final Logger.Log LOG = Logger.newLog(DBUtility.class);

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

  public static boolean checkDuplicateField(Database db, String tableName, String fieldName, String value) {
    return checkDuplicateField(db.getConnection(), tableName, fieldName, value);
  }

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

  public static String getPasswordFromUsername(Database db, String username) {
    return getPasswordFromUsername(db.getConnection(), username);
  }

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

  public static String getUuidFromName(Database db, String table, String name) {
    return getUuidFromName(db.getConnection(), table, name);
  }


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

  public static String getNameFromUuid(Database db, String table, String uuid) {
    return getNameFromUuid(db.getConnection(), table, uuid);
  }

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

  public static ArrayList<String> getAllChatroomNames(Database db) {
    return getAllChatroomNames(db.getConnection());
  }

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

  public static User addUser(Database db, String username, String password) {
    return addUser(db.getConnection(), username, password);
  }

  public static Conversation addConversation(Connection conn, String title, String ownerID) {
    try {
      // add conversation to database
      Conversation newConv = Models.newConversation(title, Uuid.parse(ownerID));
      String sqlQuery = "INSERT OR IGNORE INTO chatrooms(uuid, name) VALUES (?, ?)";
      PreparedStatement insert = conn.prepareStatement(sqlQuery);
      insert.setString(1, newConv.id.toString());
      insert.setString(2, title);
      insert.executeUpdate();
      insert.close();
      conn.close();
      return newConv;
    } catch (IOException e) {
      LOG.error("Error creating UUID for conversation" + e);
      return null;
    } catch (SQLException e) {
      LOG.error("Error adding conversation to database " + e);
      return null;
    }
  }

  public static Conversation addConversation(Database db, String title, String ownerID) {
    return addConversation(db.getConnection(), title, ownerID);
  }

}
