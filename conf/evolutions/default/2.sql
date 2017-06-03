# --- !Ups

CREATE TABLE chatrooms (
  'name' VARCHAR PRIMARY KEY,
  'owner' VARCHAR
);

CREATE TABLE messages (
  'roomname' VARCHAR,
  'author_uuid' VARCHAR,
  'message' VARCHAR,
  'time' VARCHAR,
  FOREIGN KEY ('roomname') REFERENCES chatrooms(name),
  FOREIGN KEY ('author_uuid') REFERENCES users(uuid)
);

CREATE TABLE room_permissions (
  'roomname' VARCHAR,
  'user' VARCHAR
);

# --- !Downs
DROP TABLE chatrooms;
DROP TABLE messages;
DROP TABLE room_permissions;