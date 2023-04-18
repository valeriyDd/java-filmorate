package ru.yandex.practicum.filmorate.storage.like;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

@Component
@Slf4j
public class LikesStorage {
    private final JdbcTemplate jdbcTemplate;
    private final FilmDbStorage filmDbStorage;
    private final UserDbStorage userDbStorage;

    public LikesStorage(JdbcTemplate jdbcTemplate,
                        FilmDbStorage filmDbStorage, UserDbStorage userDbStorage) {
        this.jdbcTemplate = jdbcTemplate;
        this.filmDbStorage = filmDbStorage;
        this.userDbStorage = userDbStorage;
    }

    public void addLike(int id, int userId) {
        if (!filmDbStorage.isFilmExists(id)) throw new NotFoundException("Film not found");
        if (!userDbStorage.isUserExists(userId)) throw new NotFoundException("User not found");
        String sql = "INSERT INTO LIKES (user_id, film_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, userId, id);
        log.info("Пользователь с id {} добавил отметку к фильму с id {}", userId, id);
    }

    public void removeLike(int id, int userId) {
        if (!filmDbStorage.isFilmExists(id)) throw new NotFoundException("Film not found");
        if (!userDbStorage.isUserExists(userId)) throw new NotFoundException("User not found");
        if (!isLikeExist(userId, id)) throw new NotFoundException("User didn't add like to film");
        String sql = "DELETE FROM LIKES WHERE user_id = ? AND film_id = ?";
        jdbcTemplate.update(sql, userId, id);
        log.info("Пользователь с id {} убрал отметку к фильму с id {}", userId, id);
    }

    private boolean isLikeExist(int userId, int filmId) {
        log.info("Проверка отметки к фильму с id {}", filmId);
        String sql = "SELECT * FROM LIKES WHERE user_id = ? AND film_id = ?";
        SqlRowSet userRows = jdbcTemplate.queryForRowSet(sql, userId, filmId);
        return userRows.next();
    }

    public List<Film> getPopular(int count) {
        String sql = "SELECT FILMS.FILM_ID, FILMS.NAME, FILMS.DESCRIPTION, FILMS.RELEASEDATE, FILMS.DURATION, FILMS.RATE_ID , " +
                "COUNT(L.USER_ID) as RATING, R.MPA_NAME as MPA_NAME " +
                "FROM FILMS " +
                "LEFT JOIN LIKES as L on FILMS.FILM_ID = L.FILM_ID " +
                "LEFT JOIN RATES_MPA as R ON FILMS.RATE_ID = R.MPA_ID " +
                "GROUP BY FILMS.FILM_ID " +
                "ORDER BY RATING DESC LIMIT ?";
        List<Film> films = jdbcTemplate.query(sql, (rs, rowNum) -> new Film(
                rs.getInt("film_id"),
                rs.getString("name"),
                rs.getString("description"),
                rs.getDate("releaseDate").toLocalDate(),
                rs.getInt("duration"),
                findFilmGenres(rs.getInt("film_id")),
                new Mpa(rs.getInt("rate_id"), rs.getString("mpa_name")),
                rs.getInt("rating")
        ), count);
        log.info("Получение списка популярных фильмов");
        return films;
    }

    private Set<Genre> findFilmGenres(int filmId) {
        Set<Genre> genres = new TreeSet<>();
        String sql = "SELECT * FROM GENRES AS g JOIN FILM_GENRES AS fg ON g.genre_id = fg.genre_id WHERE fg.film_id = ?";
        SqlRowSet rows = jdbcTemplate.queryForRowSet(sql, filmId);
        while (rows.next()) {
            genres.add(new Genre(rows.getInt("genre_id"), rows.getString("genre")));
        }
        log.info("Данные по жанрам получены");
        return genres;
    }
}
