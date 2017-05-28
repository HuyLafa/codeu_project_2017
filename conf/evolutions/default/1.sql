# --- !Ups

CREATE TABLE users (
  'id' INTEGER PRIMARY KEY,
  'username' VARCHAR UNIQUE NOT NULL,
  'password' VARCHAR NOT NULL,
  'uuid' VARCHAR
);

CREATE UNIQUE INDEX username_index ON users ('username');

INSERT INTO users ('username', 'password', 'uuid') VALUES ("admin", "123456", "0");

# --- !Downs
DROP TABLE users;