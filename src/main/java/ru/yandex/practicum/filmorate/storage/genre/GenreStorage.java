package ru.yandex.practicum.filmorate.storage.genre;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

@Component
@Slf4j
public class GenreStorage {
    JdbcTemplate jdbcTemplate;

    public GenreStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Set<Genre> getFilmGenres(int filmId) {
        String sql = "SELECT GENRES.GENRE_ID, GENRE FROM FILM_GENRES JOIN GENRES " +
                "ON FILM_GENRES.GENRE_ID = GENRES.GENRE_ID " +
                "WHERE FILM_ID = ?";
        log.info("Получение жанров");
        return new TreeSet<>(jdbcTemplate.query(sql, (rs, rowNum) -> new Genre(
                        rs.getInt("genre_id"),
                        rs.getString("genre")),
                filmId
        ));
    }

    public void updateGenresOfFilm(Film film) {
        jdbcTemplate.update("DELETE FROM FILM_GENRES WHERE film_id = ?", film.getId());
        if (film.getGenres() != null) {
            String sqlToFilmGenre = "INSERT INTO FILM_GENRES (genre_id, film_id) VALUES(?, ?)";
            List<Genre> genres = List.copyOf(film.getGenres());
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
        log.info("Обновление жанров");
    }

    public Collection<Genre> getGenres() {
        log.info("Получение жанров");
        return jdbcTemplate.query("SELECT * FROM GENRES",
                ((rs, rowNum) -> new Genre(
                        rs.getInt("genre_id"),
                        rs.getString("genre"))
                ));
    }

    public Genre getGenreById(int id) {
        SqlRowSet userRows = jdbcTemplate.queryForRowSet("SELECT GENRE FROM GENRES WHERE GENRE_ID = ?", id);
        if (userRows.next()) {
            Genre genre = new Genre(
                    id,
                    userRows.getString("genre")
            );
            log.info("Получение жанра {}", genre);
            return genre;
        } else {
            throw new NotFoundException(String.format("Нет жанра с id %d", id));
        }
    }
}
