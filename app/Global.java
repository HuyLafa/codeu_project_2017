import codeu.chat.ServerMain;
import codeu.chat.common.User;
import models.DBUtility;
import play.GlobalSettings;
import play.Application;

import java.sql.DriverManager;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Play's Global class, which runs custom code when the application starts / ends.
 */
public class Global extends GlobalSettings {

  /**
   * Setting up the application when it starts by adding default user and chatrooms to the database.
   * @param app this application.
   */
  public void onStart(Application app) {

    // run "sh run_server.sh" in background

    ExecutorService service = Executors.newFixedThreadPool(4);
    service.submit(new Runnable() {
      public void run() {
        (new ServerMain()).runServer();
      }
    });

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

      // create two default public rooms
      DBUtility.addConversation(DriverManager.getConnection(url), "public");
      DBUtility.addConversation(DriverManager.getConnection(url), "room1");

    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
