# --- !Ups

CREATE TABLE chatrooms (
  'name' VARCHAR PRIMARY KEY
);

CREATE TABLE messages (
  'roomname' VARCHAR,
  'author_uuid' VARCHAR,
  'message' VARCHAR,
  'time' VARCHAR,
  FOREIGN KEY ('roomname') REFERENCES chatrooms(name),
  FOREIGN KEY ('author_uuid') REFERENCES users(uuid)
)

# --- !Downs
DROP TABLE chatrooms;
DROP TABLE messages;