# --- !Ups

CREATE TABLE chatrooms (
  'name' VARCHAR PRIMARY KEY,
  'owner' VARCHAR DEFAULT 'admin'
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

CREATE UNIQUE INDEX permission ON room_permissions('roomname', 'user');

# --- !Downs
DROP TABLE chatrooms;
DROP TABLE messages;
DROP TABLE room_permissions;