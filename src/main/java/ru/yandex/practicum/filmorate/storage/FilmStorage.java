package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;

public interface FilmStorage {
    Film createFilm(Film film);

    Film updateFilm(Film film);

    void deleteFilm(Integer id);

    List<Film> getAllFilms();

    Film getFilm(Integer id);

    void like(Integer filmId, Integer userId);

    void dislike(Integer filmId, Integer userId);

    List<Film> getPopularFilms(Integer count);

    List<Genre> getAllGenres();

    Genre getGenre(Integer id);

    List<Mpa> getAllMpa();

    Mpa getMpa(Integer id);
}
