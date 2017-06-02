package models;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import play.db.Database;
import play.db.Databases;
import play.db.evolutions.Evolutions;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Test database methods
 */
public class DBUtilityTest {
  Database db;

  @Before
  public void createDatabase() throws Exception {
    db = Databases.createFrom("org.sqlite.JDBC", "jdbc:sqlite:testdb.db");
    Evolutions.applyEvolutions(db);
  }

  @After
  public void shutdownDatabase() {
    Evolutions.cleanupEvolutions(db);
    db.shutdown();
  }

  @Test
  public void testAddUser() throws SQLException {

    // add a general user
    assertEquals(1, DBUtility.addUser(db, "testuser1", "password1", "123456"));
    Connection conn = db.getConnection();
    String testQuery = "SELECT * FROM users WHERE name = 'testuser1' AND password = 'password1' AND uuid = '123456'";
    assertTrue(conn.prepareStatement(testQuery).executeQuery().next());
    conn.close();

    // duplicate uuid does not add
    assertEquals(0, DBUtility.addUser(db, "testuser2", "password2", "123456"));
    conn = db.getConnection();
    testQuery = "SELECT * FROM users WHERE name = 'testuser2' AND password = 'password2' AND uuid = '123456'";
    assertFalse(conn.prepareStatement(testQuery).executeQuery().next());
    conn.close();

    // duplicate username does not add
    assertEquals(0, DBUtility.addUser(db, "testuser1", "password2", "1000"));
    conn = db.getConnection();
    testQuery = "SELECT * FROM users WHERE name = 'testuser1' AND password = 'password2' AND uuid = '1000'";
    assertFalse(conn.prepareStatement(testQuery).executeQuery().next());
    conn.close();

    // null password does not add
    assertEquals(0, DBUtility.addUser(db, "testuser3", null, "1000"));
    conn = db.getConnection();
    testQuery = "SELECT * FROM users WHERE name = 'testuser3' AND password = '' AND uuid = '1000'";
    assertFalse(conn.prepareStatement(testQuery).executeQuery().next());
    conn.close();

  }

  @Test
  public void testAddConversation() throws SQLException {

    // add a general chatroom
    assertEquals(1, DBUtility.addConversation(db, "room1"));
    Connection conn = db.getConnection();
    String testQuery = "SELECT * FROM chatrooms WHERE name = 'room1'";
    assertTrue(conn.prepareStatement(testQuery).executeQuery().next());
    conn.close();

    // duplicate name does not add
    assertEquals(0, DBUtility.addConversation(db, "room1"));
    conn = db.getConnection();
    testQuery = "SELECT * FROM chatrooms WHERE name = 'room1'";
    ResultSet result = conn.prepareStatement(testQuery).executeQuery();
    assertTrue(result.next());
    assertFalse(result.next());
    conn.close();

  }

  @Test
  public void testCheckDuplicateField() {

    DBUtility.addUser(db, "testuser1", "password1", "123456");
    assertTrue(DBUtility.checkDuplicateField(db, "users", "name", "testuser1"));
    assertTrue(DBUtility.checkDuplicateField(db, "users", "password", "password1"));
    assertTrue(DBUtility.checkDuplicateField(db, "users", "uuid", "123456"));
    assertFalse(DBUtility.checkDuplicateField(db, "users", "name", "testuser2"));

  }

  @Test
  public void testGetPasswordFromUsername() {

    // successful query
    DBUtility.addUser(db, "testuser1", "password1", "123456");
    assertEquals("password1", DBUtility.getPasswordFromUsername(db, "testuser1"));

    // username does not exist
    assertNull(DBUtility.getPasswordFromUsername(db, "user1000"));

  }

  @Test
  public void testGetUuidFromName() {

    // successful query
    DBUtility.addUser(db, "testuser1", "password1", "123456");
    assertEquals("123456", DBUtility.getUuidFromName(db, "users", "testuser1"));

    // username does not exist
    assertNull(DBUtility.getUuidFromName(db, "users", "user1000"));

  }

  @Test
  public void testGetNameFromUuid() {

    // successful query
    DBUtility.addUser(db, "testuser1", "password1", "123456");
    assertEquals("testuser1", DBUtility.getNameFromUuid(db, "users", "123456"));

    // uuid does not exist
    assertNull(DBUtility.getNameFromUuid(db, "users", "1234567"));

  }

  @Test
  public void testGetAllChatroomNames() {

    // initially no chatroom
    assertEquals(0, DBUtility.getAllChatroomNames(db).size());

    // successful add
    DBUtility.addConversation(db, "room1");
    assertEquals(1, DBUtility.getAllChatroomNames(db).size());
    DBUtility.addConversation(db, "room2");
    assertEquals(2, DBUtility.getAllChatroomNames(db).size());

    // unsuccessful add
    DBUtility.addConversation(db, "room2");
    assertEquals(2, DBUtility.getAllChatroomNames(db).size());

  }

  @Test
  public void testGetAllMessages() {
    // not used
  }
}
