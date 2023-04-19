package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.likes.LikesStorage;

import java.util.Collection;
import java.util.List;

@Service
public class FilmService {

    private final FilmStorage filmStorage;
    private final LikesStorage likesStorage;

    @Autowired
    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage, LikesStorage likesStorage) {
        this.likesStorage = likesStorage;
        this.filmStorage = filmStorage;
    }

    public Collection<Film> findAllFilms() {
        return filmStorage.getFilms();
    }

    public Film createFilm(Film film) {
        return filmStorage.addFilm(film);
    }

    public Film updateFilm(Film film) {
        return filmStorage.updateFilm(film);
    }

    public Film getFilm(int id) {
        if (filmStorage.getFilm(id) == null) {
            throw new IllegalArgumentException("неправильный id");
        }
        return filmStorage.getFilm(id);
    }

    public void addLike(int filmId, int userId) {
        likesStorage.addLike(filmId, userId);
    }

    public void deleteLike(int filmId, int userId) {
        likesStorage.deleteLike(filmId, userId);
    }

    public List<Film> getTopFilms(int count) {
        return likesStorage.getTopFilms(count);
    }
}