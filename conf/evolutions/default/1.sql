# --- !Ups

CREATE TABLE users (
  'uuid' VARCHAR PRIMARY KEY,
  'username' VARCHAR UNIQUE NOT NULL,
  'password' VARCHAR NOT NULL,
  'email' VARCHAR
);

CREATE UNIQUE INDEX username_index ON users ('username');

# --- !Downs
DROP TABLE users;