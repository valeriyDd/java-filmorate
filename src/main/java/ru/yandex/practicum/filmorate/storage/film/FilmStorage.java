package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;

public interface FilmStorage {

    Film getFilm(int id);

    Collection<Film> getFilms();

    Film addFilm(Film film);

    Film removeFilm(Film film);

    Film updateFilm(Film film);
}
