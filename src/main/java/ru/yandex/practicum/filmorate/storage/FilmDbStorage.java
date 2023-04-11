package ru.yandex.practicum.filmorate.storage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.*;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;

@Component
@Qualifier
public class FilmDbStorage implements FilmStorage {

    @Autowired
    private final JdbcTemplate jdbcTemplate;
    private final String insertSQL = "INSERT INTO films (name) VALUES(?);";

    public FilmDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Film createFilm(Film film) {
        Integer idMpa;
        if (film.getMpa() != null) {
            idMpa = film.getMpa().getId();
        } else {
            idMpa = null;
        }
        String massage = film.getName();
        Integer id = insertMessage(massage);
        jdbcTemplate.update("UPDATE films SET description = ?, duration = ?, mpa = ?, release_date = ? WHERE " +
                        "id = ?;", film.getDescription(), film.getDuration(), idMpa, film.getReleaseDate().toString(),
                id);
        if (film.getGenres() != null) {
            Set<Genre> genresWithoutDoubles = new HashSet<>(film.getGenres());
            for (Genre genre: genresWithoutDoubles) {
                jdbcTemplate.update("INSERT INTO films_in_genres (film_id, genre_id) VALUES(?, ?);", id,
                        genre.getId());
            }
        }
        return getFilm(id);
    }

    @Override
    public Film updateFilm(Film film) {
        isPresentFilm(film.getId());
        jdbcTemplate.update("DELETE FROM films_in_genres WHERE film_id = ?;", film.getId());
        if (film.getGenres() != null) {
            Set<Genre> genresWithoutDoubles = new HashSet<>(film.getGenres());
            for (Genre genre: genresWithoutDoubles) {
                jdbcTemplate.update("INSERT INTO films_in_genres (film_id, genre_id) VALUES(?, ?);", film.getId(),
                        genre.getId());
            }
        }
        Integer idMpa;
        if (film.getMpa() != null) {
            idMpa = film.getMpa().getId();
        } else {
            idMpa = null;
        }
        jdbcTemplate.update("UPDATE films SET name = ?, description = ?, duration = ?, mpa = ?, " +
                            "release_date = ? WHERE id = ?;", film.getName(), film.getDescription(),
                film.getDuration(), idMpa, film.getReleaseDate().toString(), film.getId());
        return getFilm(film.getId());
    }

    @Override
    public List<Film> getAllFilms() {
        SqlRowSet selectAllFilms = jdbcTemplate.queryForRowSet("SELECT * FROM films;");
        List<Film> films = new ArrayList<>();
        while (selectAllFilms.next()) {
            films.add(newFilm(selectAllFilms));
        }
        return films;
    }

    @Override
    public void deleteFilm(Integer id) {
        isPresentFilm(id);
        jdbcTemplate.update("DELETE FROM films_in_genres WHERE film_id = ?;", id);
        jdbcTemplate.update("DELETE FROM likes WHERE film_id = ?;", id);
        jdbcTemplate.update("DELETE FROM films WHERE id = ?;", id);
    }

    @Override
    public Film getFilm(Integer id) {
        SqlRowSet selectFilm = isPresentFilm(id);
        return newFilm(selectFilm);
    }

    @Override
    public void like(Integer filmId, Integer userId) {
        isPresentFilm(filmId);
        SqlRowSet selectUser = jdbcTemplate.queryForRowSet("SELECT * FROM users WHERE id = ?;", userId);
        if (selectUser.next()) {
            jdbcTemplate.update("INSERT INTO likes (user_id, film_id) VALUES(?, ?);", userId, filmId);
        } else {
            throw new NotFoundException();
        }
    }

    @Override
    public void dislike(Integer filmId, Integer userId) {
        isPresentFilm(filmId);
        SqlRowSet selectUser = jdbcTemplate.queryForRowSet("SELECT * FROM users WHERE id = ?;", userId);
        if (selectUser.next()) {
            jdbcTemplate.update("DELETE FROM likes WHERE user_id = ? AND film_id = ?;", userId, filmId);
        } else {
            throw new NotFoundException();
        }
    }

    @Override
    public List<Film> getPopularFilms(Integer count) {
        SqlRowSet selectFilms = jdbcTemplate.queryForRowSet("SELECT * FROM films WHERE id IN (SELECT film_id " +
                "FROM (SELECT COUNT(user_id), film_id FROM likes GROUP BY film_id ORDER BY film_id DESC LIMIT ?));",
                count);
        List<Film> popularFilms = new ArrayList<>();
        while (selectFilms.next()) {
            popularFilms.add(newFilm(selectFilms));
        }
        if (popularFilms.size() < count) {
            selectFilms = jdbcTemplate.queryForRowSet("SELECT * FROM films ORDER BY name LIMIT ?",
                    count - popularFilms.size());
            while (selectFilms.next()) {
                popularFilms.add(newFilm(selectFilms));
            }
        }
        return popularFilms;
    }

    @Override
    public List<Genre> getAllGenres() {
        SqlRowSet selectGenres = jdbcTemplate.queryForRowSet("SELECT * FROM genres");
        List<Genre> allGenres = new ArrayList<>();
        while (selectGenres.next()) {
            allGenres.add(new Genre(selectGenres.getInt("id"), selectGenres.getString("name")));
        }
        return allGenres;
    }

    @Override
    public Genre getGenre(Integer id) {
        SqlRowSet selectGenres = jdbcTemplate.queryForRowSet("SELECT * FROM genres WHERE id = ?", id);
        if (selectGenres.next()) {
            return new Genre(selectGenres.getInt("id"), selectGenres.getString("name"));
        } else {
            throw new NotFoundException();
        }
    }

    @Override
    public List<Mpa> getAllMpa() {
        SqlRowSet selectMpa = jdbcTemplate.queryForRowSet("SELECT * FROM Mpa");
        List<Mpa> allMpa = new ArrayList<>();
        while (selectMpa.next()) {
            allMpa.add(new Mpa(selectMpa.getInt("id"), selectMpa.getString("name")));
        }
        return allMpa;
    }

    @Override
    public Mpa getMpa(Integer id) {
        SqlRowSet selectMpa = jdbcTemplate.queryForRowSet("SELECT * FROM Mpa WHERE id = ?", id);
        if (selectMpa.next()) {
            return new Mpa(selectMpa.getInt("id"), selectMpa.getString("name"));
        } else {
            throw new NotFoundException();
        }
    }

    private Integer insertMessage(String message) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(insertSQL, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, message);
            return ps;
        }, keyHolder);

        return (Integer) keyHolder.getKey();
    }

    private SqlRowSet isPresentFilm(Integer id) {
        SqlRowSet selectMpa = jdbcTemplate.queryForRowSet("SELECT * FROM films WHERE id = ?", id);
        if (selectMpa.next()) {
            return selectMpa;
        } else {
            throw new NotFoundException();
        }
    }

    private Film newFilm(SqlRowSet selectFilm) {
        SqlRowSet selectGenres = jdbcTemplate.queryForRowSet("SELECT * FROM genres WHERE id IN (SELECT genre_id " +
                "FROM films_in_genres WHERE film_id = ?);", selectFilm.getInt("id"));
        List<Genre> genres = new ArrayList<>();
        while (selectGenres.next()) {
            genres.add(new Genre(selectGenres.getInt("id"), selectGenres.getString("name")));
        }
        Mpa mpa = new Mpa();
        mpa.setId(selectFilm.getInt("mpa"));
        SqlRowSet selectNameOfMpa = jdbcTemplate.queryForRowSet("SELECT name FROM Mpa WHERE id = ?;",
                selectFilm.getInt("mpa"));
        selectNameOfMpa.next();
        mpa.setName(selectNameOfMpa.getString("name"));
        Film film = new Film(
                selectFilm.getInt("id"),
                selectFilm.getString("name"),
                selectFilm.getString("description"),
                selectFilm.getInt("duration"),
                genres,
                mpa,
                selectFilm.getDate("release_date").toLocalDate());
        return film;
    }
}
