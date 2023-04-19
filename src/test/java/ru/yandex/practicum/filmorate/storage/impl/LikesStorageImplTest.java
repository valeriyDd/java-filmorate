package ru.yandex.practicum.filmorate.storage.impl;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.MPA;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.likes.LikesStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class LikesStorageImplTest {

    private final LikesStorage likesStorage;
    private final FilmDbStorage filmStorage;
    private final UserDbStorage userStorage;
    Film film;
    Film film2;
    User user;
    User user2;

    @BeforeEach
    void beforeAll() {
        film = Film.builder().name("name").description("description")
                .releaseDate(LocalDate.of(2000, 1, 1)).duration(100)
                .mpa(new MPA(1, "G")).build();
        film2 = Film.builder().name("name2").description("description2")
                .releaseDate(LocalDate.of(2003, 4, 5)).duration(10)
                .mpa(new MPA(1, "G")).build();
        user = User.builder().email("email@yandex.ru").login("login").name("name")
                .birthday(LocalDate.of(2000, 1, 1)).build();
        user2 = User.builder().email("email2@yandex.ru").login("login2").name("name2")
                .birthday(LocalDate.of(2001, 2, 2)).build();
        filmStorage.addFilm(film);
        filmStorage.addFilm(film2);
        userStorage.addUser(user);
        userStorage.addUser(user2);
    }

    @Test
    @DirtiesContext
    void addLike() {
        likesStorage.addLike(film.getId(), user.getId());
        assertEquals(film.getId(), likesStorage.getTopFilms(1).get(0).getId());
        assertEquals(1, likesStorage.getTopFilms(1).size());
    }

    @Test
    @DirtiesContext
    void deleteLike() {
        likesStorage.addLike(film.getId(), user.getId());
        likesStorage.addLike(film.getId(), user2.getId());
        likesStorage.addLike(film2.getId(), user2.getId());
        assertEquals(film.getId(), likesStorage.getTopFilms(1).get(0).getId());
        likesStorage.deleteLike(film.getId(), user.getId());
        likesStorage.deleteLike(film.getId(), user2.getId());
        assertEquals(film2.getId(), likesStorage.getTopFilms(1).get(0).getId());
    }

    @Test
    @DirtiesContext
    void getTopFilms() {
        likesStorage.addLike(film.getId(), user.getId());
        likesStorage.addLike(film.getId(), user2.getId());
        likesStorage.addLike(film2.getId(), user2.getId());
        assertEquals(film.getId(), likesStorage.getTopFilms(2).get(0).getId());
        assertEquals(film2.getId(), likesStorage.getTopFilms(2).get(1).getId());
    }
}