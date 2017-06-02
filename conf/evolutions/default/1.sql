# --- !Ups

CREATE TABLE users (
  'uuid' VARCHAR PRIMARY KEY,
  'name' VARCHAR UNIQUE NOT NULL,
  'password' VARCHAR NOT NULL,
  'email' VARCHAR
);

CREATE UNIQUE INDEX username_index ON users ('name');

# --- !Downs
DROP TABLE users;