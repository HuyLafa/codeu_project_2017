import codeu.chat.common.User;
import models.DBUtility;
import play.GlobalSettings;
import play.Application;

import java.sql.DriverManager;

/**
 * Created by HuyNguyen on 5/29/17.
 */
public class Global extends GlobalSettings {

  public void onStart(Application app) {
    // connect to the database
    try {
      String url = "jdbc:sqlite:./conf/chatapp.db";
      String driver = "org.sqlite.JDBC";
      Class.forName(driver);

      // set up initial database if it's empty
      boolean adminExists = DBUtility.checkDuplicateField(DriverManager.getConnection(url), "users", "name", "admin");
      if (!adminExists) {
        // create a default admin account
        User admin = DBUtility.addUser(DriverManager.getConnection(url), "admin", "123456");
      }

      System.out.println("hello world");
      // create two default public rooms
      DBUtility.addConversation(DriverManager.getConnection(url), "public");
      DBUtility.addConversation(DriverManager.getConnection(url), "room1");

    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
