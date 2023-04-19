package ru.yandex.practicum.filmorate.storage.genres;

import ru.yandex.practicum.filmorate.model.Genres;

import java.util.List;

public interface GenresStorage {
    List<Genres> getGenresByFilmId(int id);

    Genres getGenreById(int id);

    List<Genres> getGenres();

    void addFilmGenre(int filmId, int genreId);

    void deleteFilmGenres(int filmId);
}
