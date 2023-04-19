package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Slf4j
@Component("filmDbStorage")
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;
    private final GenreStorage genreStorage;
    public final MpaStorage mpaStorage;

    public FilmDbStorage(JdbcTemplate jdbcTemplate,
                         GenreStorage genreStorage,
                         MpaStorage mpaStorage) {
        this.jdbcTemplate = jdbcTemplate;
        this.genreStorage = genreStorage;
        this.mpaStorage = mpaStorage;
    }

    @Override
    public Film add(Film film) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("FILMS")
                .usingGeneratedKeyColumns("film_id");
        if (film.getGenres() != null) {
            List<Genre> genres = List.copyOf(film.getGenres());
            long nullIdGenres = genres.stream().filter(genre -> genre.getId() == null).count();
            if (nullIdGenres > 0) {
                throw new NotFoundException("id жанра не указан");
            }
            film.setId(simpleJdbcInsert.executeAndReturnKey(film.toMap()).intValue());
            String sqlToFilmGenre = "INSERT INTO FILM_GENRES (genre_id, film_id) VALUES(?, ?)";
            jdbcTemplate.batchUpdate(sqlToFilmGenre, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    ps.setInt(1, genres.get(i).getId());
                    ps.setInt(2, film.getId());
                }

                @Override
                public int getBatchSize() {
                    return genres.size();
                }
            });
        }

        if (film.getMpas() != null) {
            List<Mpa> mpas = List.copyOf(film.getMpas());
            long nullIdMpas = mpas.stream().filter(mpa -> mpa.getId() == null).count();
            if (nullIdMpas > 0) {
                throw new NotFoundException("id рейтинга не указан");
            }
            film.setId(simpleJdbcInsert.executeAndReturnKey(film.toMap()).intValue());
            String sqlToFilmGenre = "INSERT INTO FILM_GENRES (genre_id, film_id) VALUES(?, ?)";
            jdbcTemplate.batchUpdate(sqlToFilmGenre, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    ps.setInt(1, mpas.get(i).getId());
                    ps.setInt(2, film.getId());
                }

                @Override
                public int getBatchSize() {
                    return mpas.size();
                }
            });
        }
        jdbcTemplate.update("UPDATE FILMS SET RATE_ID = ? WHERE FILM_ID = ?",
                film.getMpa().getId(),
                film.getId()
        );

        log.info("Новый фильм добавлен: {}", film);
        return film;
    }

    @Override
    public Film update(Film film) {
        if (isFilmExists(film.getId())) {
            String sqlQuery = "UPDATE FILMS SET " +
                    "name = ?, description = ?, releaseDate = ?, duration = ?, " +
                    "rate_id = ? WHERE film_id = ?";
            jdbcTemplate.update(sqlQuery,
                    film.getName(),
                    film.getDescription(),
                    film.getReleaseDate(),
                    film.getDuration(),
                    film.getMpa().getId(),
                    film.getId());
            genreStorage.updateGenresOfFilm(film);
            log.info("Фильм {} обновлен", film);
            return film;
        } else {
            throw new NotFoundException(String.format("Фильм с %d не найден", film.getId()));
        }
    }

    @Override
    public List<Film> findAllFilms() {
        String sql = "SELECT * FROM FILMS LEFT JOIN FILM_GENRES AS FM ON FILMS.film_id = FM.film_id" +
                " LEFT JOIN GENRES AS G ON FM.genre_id = G.genre_id";
        List<Film> films = jdbcTemplate.query(sql, (rs, rowNum) -> makeFilm(rs));
        log.info("Получение фильмов");
        return films;
    }

    @Override
    public Optional<Film> getFilm(int id) {
        SqlRowSet userRows = jdbcTemplate.queryForRowSet("SELECT * FROM FILMS " +
                "LEFT JOIN RATES_MPA as R ON FILMS.RATE_ID = R.MPA_ID " +
                "WHERE film_id = ?", id);

        if (userRows.next()) {
            Film film = new Film(
                    userRows.getInt("film_id"),
                    userRows.getString("name"),
                    userRows.getString("description"),
                    userRows.getDate("releaseDate").toLocalDate(),
                    userRows.getInt("duration"),
                    new Mpa(userRows.getInt("rate_id"), userRows.getString("mpa_name"))
            );
            Set<Genre> genres = genreStorage.getFilmGenres(id);
            if (genres.size() != 0) {
                film.setGenres(genreStorage.getFilmGenres(id));
            }
            log.info("Фильм с id {} найден", id);
            return Optional.of(film);
        } else {
            log.error("Фильм с id {} не найден", id);
            return Optional.empty();
        }
    }

    @Override
    public Film delete(Film film) {
        if (isFilmExists(film.getId())) {
            String sql = "DELETE FROM FILMS WHERE film_id = ?";
            jdbcTemplate.update(sql, film.getId());
            log.info("Фильм {} удален", film);
            return film;
        } else {
            log.error("Фильм не найден");
            throw new NotFoundException("Фильм не найден");
        }
    }

    public boolean isFilmExists(int id) {
        log.info("Проверка фильма");
        String sql = "SELECT * FROM FILMS WHERE film_id = ?";
        SqlRowSet userRows = jdbcTemplate.queryForRowSet(sql, id);
        return userRows.next();
    }

    private Film makeFilm(ResultSet rs) throws SQLException {
        Film film = new Film();
        film.setId(rs.getInt("film_id"));
        film.setName(rs.getString("name"));
        film.setDescription(rs.getString("description"));
        film.setReleaseDate(rs.getDate("releaseDate").toLocalDate());
        film.setDuration(rs.getInt("duration"));
        String sql1 = "SELECT * FROM GENRES AS g JOIN FILM_GENRES AS fg ON g.genre_id = fg.genre_id" +
                " WHERE film_id=?";
        Set<Genre> genres = new TreeSet<>();
        SqlRowSet rows = jdbcTemplate.queryForRowSet(sql1, film.getId());
        while (rows.next()) {
            genres.add(new Genre(rows.getInt("genre_id"), rows.getString("genre")));
        }
        if (genres.size() != 0) {
            film.setGenres(genres);
        } else {
            film.setGenres(Collections.emptySet());
        }

        if (rs.getString("genre_id") != null) {
            Genre genre = new Genre();
            genre.setId(rs.getInt("genre_id"));
            genre.setName(rs.getString("genre"));
            genres.add(genre);
        }
        Mpa mpa = mpaStorage.getMpa(rs.getInt("rate_id"));
        film.setMpa(mpa);
        return film;
    }
}
