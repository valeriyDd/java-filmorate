CREATE TABLE IF NOT EXISTS PUBLIC.USERS
(
    USER_ID  INTEGER           NOT NULL AUTO_INCREMENT,
    EMAIL    CHARACTER VARYING NOT NULL,
    LOGIN    CHARACTER VARYING NOT NULL,
    NAME     CHARACTER VARYING,
    BIRTHDAY DATE              NOT NULL,
    CONSTRAINT USERS_PK PRIMARY KEY (USER_ID),
    CONSTRAINT USERS_UN UNIQUE (LOGIN, EMAIL)
);
CREATE UNIQUE INDEX IF NOT EXISTS PRIMARY_KEY_U ON PUBLIC.USERS (USER_ID);
CREATE UNIQUE INDEX IF NOT EXISTS USERS_UN_INDEX ON PUBLIC.USERS (EMAIL, LOGIN);

CREATE TABLE IF NOT EXISTS PUBLIC.FRIENDS
(
    USER_ID   INTEGER NOT NULL,
    FRIEND_ID INTEGER NOT NULL,
    PRIMARY KEY (USER_ID, FRIEND_ID),
    CONSTRAINT FRIENDS_FK FOREIGN KEY (USER_ID) REFERENCES PUBLIC.USERS (USER_ID) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT FRIENDS_FK_1 FOREIGN KEY (FRIEND_ID) REFERENCES PUBLIC.USERS (USER_ID) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS PUBLIC.MPA
(
    MPA_ID INTEGER           NOT NULL AUTO_INCREMENT,
    NAME   CHARACTER VARYING NOT NULL,
    CONSTRAINT MPA_PK PRIMARY KEY (MPA_ID)
);
CREATE UNIQUE INDEX IF NOT EXISTS PRIMARY_KEY_MPA ON PUBLIC.MPA (MPA_ID);

CREATE TABLE IF NOT EXISTS PUBLIC.FILMS
(
    FILM_ID      INTEGER                NOT NULL AUTO_INCREMENT,
    NAME         CHARACTER VARYING      NOT NULL,
    DESCRIPTION  CHARACTER VARYING(200) NOT NULL,
    RELEASE_DATE DATE                   NOT NULL,
    DURATION     INTEGER                NOT NULL,
    MPA_ID       INTEGER                NOT NULL,
    CONSTRAINT FILMS_PK PRIMARY KEY (FILM_ID),
    CONSTRAINT FILMS_FK FOREIGN KEY (MPA_ID) REFERENCES PUBLIC.MPA (MPA_ID) ON DELETE RESTRICT ON UPDATE CASCADE
);
CREATE UNIQUE INDEX IF NOT EXISTS PRIMARY_KEY_FILMS ON PUBLIC.FILMS (FILM_ID);

CREATE TABLE IF NOT EXISTS PUBLIC.LIKES
(
    FILM_ID INTEGER NOT NULL,
    USER_ID INTEGER NOT NULL,
    PRIMARY KEY (FILM_ID, USER_ID),
    CONSTRAINT LIKES_FK FOREIGN KEY (FILM_ID) REFERENCES PUBLIC.FILMS (FILM_ID) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT LIKES_FK_1 FOREIGN KEY (USER_ID) REFERENCES PUBLIC.USERS (USER_ID) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS PUBLIC.GENRES
(
    GENRE_ID INTEGER           NOT NULL AUTO_INCREMENT,
    NAME     CHARACTER VARYING NOT NULL,
    CONSTRAINT GENRES_PK PRIMARY KEY (GENRE_ID)
);
CREATE UNIQUE INDEX IF NOT EXISTS PRIMARY_KEY_G ON PUBLIC.GENRES (GENRE_ID);

CREATE TABLE IF NOT EXISTS PUBLIC.FILM_GENRES
(
    FILM_ID  INTEGER NOT NULL,
    GENRE_ID INTEGER NOT NULL,
    PRIMARY KEY (FILM_ID, GENRE_ID),
    CONSTRAINT FILM_GENRES_FK FOREIGN KEY (GENRE_ID) REFERENCES PUBLIC.GENRES (GENRE_ID) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT FILM_GENRES_FK_1 FOREIGN KEY (FILM_ID) REFERENCES PUBLIC.FILMS (FILM_ID) ON DELETE RESTRICT ON UPDATE RESTRICT
);