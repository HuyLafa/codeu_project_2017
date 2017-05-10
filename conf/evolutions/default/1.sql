# --- !Ups

CREATE TABLE users ('id' INTEGER PRIMARY KEY, 'username' VARCHAR NOT NULL, 'password' VARCHAR NOT NULL);

INSERT INTO users ('username', 'password') VALUES ("admin", "123456");


# --- !Downs
DROP TABLE users;