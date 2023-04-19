package ru.yandex.practicum.filmorate.storage.impl;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genres;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.genres.GenresStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class GenresStorageImplTest {

    private final GenresStorage genresStorage;
    private final FilmDbStorage filmStorage;

    @Test
    @DirtiesContext
    void getGenres() {
        List<Genres> genres = genresStorage.getGenres();
        assertEquals(6, genres.size());
        assertEquals(genres.get(0), new Genres(1, "Комедия"));
        assertEquals(genres.get(1), new Genres(2, "Драма"));
        assertEquals(genres.get(2), new Genres(3, "Мультфильм"));
        assertEquals(genres.get(3), new Genres(4, "Триллер"));
        assertEquals(genres.get(4), new Genres(5, "Документальный"));
        assertEquals(genres.get(5), new Genres(6, "Боевик"));
    }

    @Test
    @DirtiesContext
    void getGenreById() {
        assertEquals(genresStorage.getGenreById(1), new Genres(1, "Комедия"));
        assertEquals(genresStorage.getGenreById(2), new Genres(2, "Драма"));
        assertEquals(genresStorage.getGenreById(3), new Genres(3, "Мультфильм"));
        assertEquals(genresStorage.getGenreById(4), new Genres(4, "Триллер"));
        assertEquals(genresStorage.getGenreById(5), new Genres(5, "Документальный"));
        assertEquals(genresStorage.getGenreById(6), new Genres(6, "Боевик"));
        assertThrows(InvalidDataAccessApiUsageException.class, () -> genresStorage.getGenreById(999));
    }

    @Test
    @DirtiesContext
    void getGenresByFilmId() {
        Film film = Film.builder().name("name").description("description")
                .releaseDate(LocalDate.of(2000, 1, 1)).duration(100)
                .mpa(new Mpa(1, "G"))
                .genres(new ArrayList<>(genresStorage.getGenres().subList(0, 2))).build();
        film = filmStorage.addFilm(film);
        assertEquals(2, genresStorage.getGenresByFilmId(film.getId()).size());
        assertEquals(film.getGenres().get(0), genresStorage.getGenreById(1));
    }

    @Test
    @DirtiesContext
    void addFilmGenre() {
        Film film = Film.builder().name("name").description("description")
                .releaseDate(LocalDate.of(2000, 1, 1)).duration(100)
                .mpa(new Mpa(1, "G"))
                .genres(new ArrayList<>(genresStorage.getGenres().subList(0, 2))).build();
        film = filmStorage.addFilm(film);
        assertEquals(2, genresStorage.getGenresByFilmId(film.getId()).size());
        assertEquals(film.getGenres().get(0), genresStorage.getGenreById(1));
        film.setGenres(genresStorage.getGenres().subList(0, 3));
        filmStorage.updateFilm(film);
        assertEquals(3, genresStorage.getGenresByFilmId(film.getId()).size());

    }

    @Test
    @DirtiesContext
    void deleteFilmGenres() {
        Film film = Film.builder().name("name").description("description")
                .releaseDate(LocalDate.of(2000, 1, 1)).duration(100)
                .mpa(new Mpa(1, "G"))
                .genres(new ArrayList<>(genresStorage.getGenres().subList(0, 3))).build();
        film = filmStorage.addFilm(film);
        assertEquals(3, genresStorage.getGenresByFilmId(film.getId()).size());
        genresStorage.deleteFilmGenres(film.getId());
        assertEquals(0, genresStorage.getGenresByFilmId(film.getId()).size());
    }
}