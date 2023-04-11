CREATE TABLE IF NOT EXISTS users (
	id INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
	email VARCHAR(40),
	login VARCHAR(40),
	name VARCHAR(40),
	birthday DATE
);

CREATE TABLE IF NOT EXISTS genres (
	id INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
	name VARCHAR(20)
);

CREATE TABLE IF NOT EXISTS MPA (
	id INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
	name VARCHAR(10)
);

CREATE TABLE IF NOT EXISTS films (
	id INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
	name VARCHAR(40),
	description VARCHAR(200),
	duration INTEGER,
	MPA INTEGER REFERENCES MPA(id),
	release_date DATE
);

CREATE TABLE IF NOT EXISTS films_in_genres (
	film_id INTEGER NOT NULL REFERENCES films(id), 
	genre_id INTEGER NOT NULL REFERENCES genres(id),
	CONSTRAINT pk PRIMARY KEY (film_id, genre_id)
);

CREATE TABLE IF NOT EXISTS friend_request (
	requester INTEGER NOT NULL REFERENCES users(id),
	recipient INTEGER NOT NULL REFERENCES users(id),
	status BOOLEAN,
	CONSTRAINT pk1 PRIMARY KEY (requester, recipient)
);

CREATE TABLE IF NOT EXISTS likes (
	user_id INTEGER NOT NULL REFERENCES users(id),
	film_id INTEGER NOT NULL REFERENCES films(id),
	CONSTRAINT pk2 PRIMARY KEY (user_id, film_id)
);