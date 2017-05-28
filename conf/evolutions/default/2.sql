# --- !Ups

CREATE TABLE chatrooms (
  'uuid' VARCHAR PRIMARY KEY,
  'name' VARCHAR NOT NULL
);

CREATE TABLE message (
  'chatroom_uuid' VARCHAR,
  'author_uuid' VARCHAR,
  'message' VARCHAR,
  FOREIGN KEY ('chatroom_uuid') REFERENCES chatrooms(uuid),
  FOREIGN KEY ('author_uuid') REFERENCES users(uuid)
)

# --- !Downs
DROP TABLE chatrooms;
DROP TABLE message;