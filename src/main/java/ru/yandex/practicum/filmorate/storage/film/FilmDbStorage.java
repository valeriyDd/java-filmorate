package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genres;
import ru.yandex.practicum.filmorate.storage.genres.GenresStorage;
import ru.yandex.practicum.filmorate.storage.likes.LikesStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;

import javax.sql.DataSource;
import java.time.LocalDate;
import java.util.*;

@Slf4j
@Repository
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert insertFilm;
    private final MpaStorage mpaStorage;
    private final GenresStorage genresStorage;
    private final LikesStorage likesStorage;

    public FilmDbStorage(JdbcTemplate jdbcTemplate, DataSource dataSource,
                         MpaStorage mpaStorage, GenresStorage genresStorage, LikesStorage likesStorage) {
        this.insertFilm = new SimpleJdbcInsert(dataSource).withTableName("FILMS")
                .usingGeneratedKeyColumns("FILM_ID");
        this.jdbcTemplate = jdbcTemplate;
        this.mpaStorage = mpaStorage;
        this.genresStorage = genresStorage;
        this.likesStorage = likesStorage;
    }

    @Override
    public Collection<Film> getFilms() {
        return jdbcTemplate.query("SELECT * FROM FILMS", (rs, rowNum) -> {
            Film f = new Film();
            f.setId(rs.getInt("FILM_ID"));
            f.setName(rs.getString("NAME"));
            f.setDescription(rs.getString("DESCRIPTION"));
            f.setReleaseDate(LocalDate.parse(rs.getString("RELEASE_DATE")));
            f.setDuration(rs.getInt("DURATION"));
            f.setMpa(mpaStorage.getMpaById(rs.getInt("MPA_ID")));
            f.setGenres(genresStorage.getGenresByFilmId(f.getId()));
            return f;
        });
    }

    @Override
    public Film addFilm(Film film) {
        if (film.getMpa().getId() == 0) throw new IllegalArgumentException("id рейтинга не указан");
        if (film.getGenres() != null) {
            for (Genres genre : film.getGenres()) {
                if (genre.getId() == 0) throw new IllegalArgumentException("id жанра не указан");
            }
        }
        Boolean check = jdbcTemplate.queryForObject("SELECT EXISTS(SELECT * FROM FILMS WHERE NAME = ? " +
                        "AND DESCRIPTION = ? AND RELEASE_DATE = ? AND DURATION = ?)",
                Boolean.class, film.getName(), film.getDescription(), film.getReleaseDate(), film.getDuration());
        if (Boolean.TRUE.equals(check)) throw new IllegalArgumentException("Этот фильм уже добавлен в базу");
        Map<String, Object> parameters = new HashMap<>(5);
        parameters.put("NAME", film.getName());
        parameters.put("DESCRIPTION", film.getDescription());
        parameters.put("RELEASE_DATE", film.getReleaseDate());
        parameters.put("DURATION", film.getDuration());
        parameters.put("MPA_ID", film.getMpa().getId());
        Number newId = insertFilm.executeAndReturnKey(parameters);
        film.setId(newId.intValue());
        film.setRating(0);
        if (film.getGenres() != null) {
            Set<Genres> genres = new LinkedHashSet<>(film.getGenres());
            film.getGenres().clear();
            film.getGenres().addAll(genres);
            for (Genres g : film.getGenres()) {
                genresStorage.addFilmGenre(film.getId(), g.getId());
            }
        }
        return film;
    }

    @Override
    public Film removeFilm(Film film) {
        jdbcTemplate.update("DELETE FROM FILMS WHERE FILM_ID=?", film.getId());
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        if (film.getId() == null) throw new IllegalArgumentException("id не указан");
        if (film.getMpa().getId() == 0) throw new IllegalArgumentException("id рейтинга не указан");
        if (film.getGenres() != null) {
            for (Genres genre : film.getGenres()) {
                if (genre.getId() == 0) throw new IllegalArgumentException("id жанра не указан");
            }
        }
        int status = jdbcTemplate.update("UPDATE FILMS SET NAME = ?, DESCRIPTION = ?, RELEASE_DATE = ?, " +
                        "DURATION = ?, MPA_ID = ? " +
                        "WHERE FILM_ID = ?", film.getName(), film.getDescription(), film.getReleaseDate(),
                film.getDuration(), film.getMpa().getId(), film.getId());
        if (film.getGenres() != null) {
            genresStorage.deleteFilmGenres(film.getId());
            Set<Genres> genres = new LinkedHashSet<>(film.getGenres());
            film.getGenres().clear();
            film.getGenres().addAll(genres);
            for (Genres g : film.getGenres()) {
                genresStorage.addFilmGenre(film.getId(), g.getId());
            }
        }
        film.setRating(likesStorage.countOfLikes(film.getId()));
        if (status != 1) {
            throw new IllegalArgumentException("неправильный id");
        }
        return film;
    }

    @Override
    public Film getFilm(int id) {
        SqlRowSet filmRows = jdbcTemplate.queryForRowSet("SELECT * FROM FILMS WHERE FILM_ID = ?", id);
        if (filmRows.next()) {
            Film f = new Film();
            f.setId(filmRows.getInt("FILM_ID"));
            f.setName(filmRows.getString("NAME"));
            f.setDescription(filmRows.getString("DESCRIPTION"));
            f.setReleaseDate(filmRows.getDate("RELEASE_DATE").toLocalDate());
            f.setDuration(filmRows.getInt("DURATION"));
            f.setMpa(mpaStorage.getMpaById(filmRows.getInt("MPA_ID")));
            f.setGenres(genresStorage.getGenresByFilmId(f.getId()));
            f.setRating(likesStorage.countOfLikes(f.getId()));
            return f;
        } else {
            throw new IllegalArgumentException("неправильный id");
        }
    }
}
