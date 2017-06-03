import codeu.chat.ServerMain;
import codeu.chat.common.User;
import models.DBUtility;
import models.Models;
import play.GlobalSettings;
import play.Application;
import play.db.Database;
import play.db.Databases;
import play.db.evolutions.Evolutions;

import java.sql.DriverManager;

/**
 * Play's Global class, which runs custom code when the application starts / ends.
 */
public class Global extends GlobalSettings {

  String url = "jdbc:sqlite:chatapp.db";
  String driver = "org.sqlite.JDBC";
  Database db = Databases.createFrom(driver, url);

  /**
   * Setting up the application when it starts by adding default user and chatrooms to the database.
   * @param app this application.
   */
  public void onStart(Application app) {

    // connect to the database
    try {
      Class.forName(driver);
      Evolutions.applyEvolutions(db);

      // set up initial database
      User admin = Models.newUser("admin");
      DBUtility.addUser(DriverManager.getConnection(url), "admin", "123456", admin.id.toString());

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void onStop(Application app) {
    Evolutions.cleanupEvolutions(db);
  }
}
