package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


/**
 * Created by HuyNguyen on 4/3/17.
 */
public class DBConnection {
  Connection con;

  /**
   * Forms connection to a database given a username and password. The url will be hardcoded to the database and currently only acts on localhost:3306.
   * Auto commit is also turned off in this method.
   *
   * @param username String username
   * @param password String password
   * @param database String database to connect to (for this project, it will either be rbttutor or rbttutorunittesting
   * @return         Connection object of the database connection
   */
  public Connection formConnection(String username, String password, String database) {
    try {
      String url = "jdbc:sqlite:./chatapp.db";
      con = DriverManager.getConnection(url, username, password);
      con.setAutoCommit(false);
      return con;
    }
    catch (SQLException e) {
      e.printStackTrace();
      return null;
    }
  }

  /**
   * Standard method to commit the transaction inside the DBConnection.Connection
   */
  public void commitTransaction() throws SQLException {
    con.commit();
  }

  /**
   * Call this to "erase" any updates or queries in the DBConnection.Connection
   */
  public void rollback() throws SQLException {
    con.rollback();
  }


  /**
   * This method closes the connection to the database.
   * It should always be called when finished interacting with the DB.
   */
  public void closeConnection() throws SQLException {
    if (con != null) {
      con.close();
    }
  }


  /**
   * Set the new schema for the table.
   * @param newSchema text containing the new schema.
   * @throws SQLException
   */
  public void setSchema(String newSchema) throws SQLException {
    con.setSchema(newSchema);
  }


  /**
   * Set the new catalog for the table.
   * @param newCatalog text containing the new catalog.
   * @throws SQLException
   */
  public void setCatalog(String newCatalog) throws SQLException {
    con.setCatalog(newCatalog);
  }


  /**
   * Get the database connection.
   * @return the connection.
   */
  public Connection getConnection() {
    return this.con;
  }
}
