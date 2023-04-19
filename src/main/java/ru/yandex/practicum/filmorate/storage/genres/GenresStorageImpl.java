package ru.yandex.practicum.filmorate.storage.genres;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Genres;

import java.util.List;

@Repository
public class GenresStorageImpl implements GenresStorage {
    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Genres> genresRowMapper = (rs, rowNum) -> {
        Genres genre = new Genres();
        genre.setId(rs.getInt("GENRE_ID"));
        genre.setName(rs.getString("NAME"));
        return genre;
    };

    public GenresStorageImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Genres> getGenres() {
        return jdbcTemplate.query("SELECT * FROM GENRES", genresRowMapper);
    }

    @Override
    public Genres getGenreById(int id) {
        SqlRowSet genresRows = jdbcTemplate.queryForRowSet("SELECT * FROM GENRES WHERE GENRE_ID = ?", id);
        if (genresRows.next()) {
            Genres g = new Genres();
            g.setId(genresRows.getInt("GENRE_ID"));
            g.setName(genresRows.getString("NAME"));
            return g;
        } else {
            throw new IllegalArgumentException("неправильный id");
        }
    }

    @Override
    public List<Genres> getGenresByFilmId(int id) {
        if (!checkFilmId(id)) throw new IllegalArgumentException("неправильный id");
        String sql = "SELECT * FROM GENRES LEFT JOIN FILM_GENRES FG on GENRES.GENRE_ID = FG.GENRE_ID " +
                "WHERE FILM_ID = " + id;
        return jdbcTemplate.query(sql, genresRowMapper);
    }

    @Override
    public void addFilmGenre(int filmId, int genreId) {
        if (filmId == 0 || genreId == 0) throw new IllegalArgumentException("id не указан");
        if (!(checkGenreId(genreId) & checkFilmId(filmId))) throw new IllegalArgumentException("неправильный id");
        int status = jdbcTemplate.update("INSERT INTO FILM_GENRES (FILM_ID, GENRE_ID) " +
                "VALUES ( ?, ? )", filmId, genreId);
        if (status != 1) throw new IllegalArgumentException("ошибка");
    }

    @Override
    public void deleteFilmGenres(int filmId) {
        jdbcTemplate.update("DELETE FROM FILM_GENRES WHERE FILM_ID = ?", filmId);
    }

    private boolean checkFilmId(int filmId) {
        Boolean checkFilm = jdbcTemplate.queryForObject("SELECT EXISTS(SELECT * FROM FILMS WHERE FILM_ID = ?)",
                Boolean.class, filmId);
        return Boolean.TRUE.equals(checkFilm);
    }

    private boolean checkGenreId(int genreId) {
        Boolean checkGenre = jdbcTemplate.queryForObject("SELECT EXISTS(SELECT * FROM GENRES WHERE GENRE_ID = ?)",
                Boolean.class, genreId);
        return Boolean.TRUE.equals(checkGenre);
    }
}
