# --- !Ups

CREATE TABLE total_message (
  'user1' VARCHAR NOT NULL,
  'user2' VARCHAR NOT NULL,
  'message_count' INT,
  PRIMARY KEY ('user1', 'user2')
);

CREATE TABLE message (
  'user1' VARCHAR NOT NULL,
  'user2' VARCHAR NOT NULL,
  'message_number' INT,
  'message' VARCHAR,
  'from_user' VARCHAR NOT NULL,
  PRIMARY KEY ('user1', 'user2', 'message_number')
);

# --- !Downs
DROP TABLE total_message;
DROP TABLE message;