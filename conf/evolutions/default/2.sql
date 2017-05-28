# --- !Ups

CREATE TABLE total_message (
  'user1_uuid' VARCHAR NOT NULL,
  'user2_uuid' VARCHAR NOT NULL,
  'message_count' INT,
  PRIMARY KEY ('user1_uuid', 'user2_uuid')
);

CREATE TABLE message (
  'user1_uuid' VARCHAR NOT NULL,
  'user2_uuid' VARCHAR NOT NULL,
  'message_number' INT,
  'message' VARCHAR,
  'from_user_uuid' VARCHAR NOT NULL,
  PRIMARY KEY ('user1_uuid', 'user2_uuid', 'message_number')
);

# --- !Downs
DROP TABLE total_message;
DROP TABLE message;