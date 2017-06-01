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

        // create two default public rooms
        String adminID = admin.id.toString();
        DBUtility.addConversation(DriverManager.getConnection(url), "public", adminID);
        DBUtility.addConversation(DriverManager.getConnection(url), "room1", adminID);
      }

    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
