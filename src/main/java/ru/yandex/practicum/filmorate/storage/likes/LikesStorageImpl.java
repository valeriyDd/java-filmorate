package ru.yandex.practicum.filmorate.storage.likes;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.genres.GenresStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;

import java.time.LocalDate;
import java.util.List;

@Repository
public class LikesStorageImpl implements LikesStorage {
    private final JdbcTemplate jdbcTemplate;
    private final MpaStorage mpaStorage;
    private final GenresStorage genresStorage;

    public LikesStorageImpl(JdbcTemplate jdbcTemplate, MpaStorage mpaStorage, GenresStorage genresStorage) {
        this.jdbcTemplate = jdbcTemplate;
        this.mpaStorage = mpaStorage;
        this.genresStorage = genresStorage;
    }

    public void addLike(int filmId, int userId) {
        if (!(checkFilmId(filmId) & checkUserId(userId))) throw new IllegalArgumentException("неправильный id");
        jdbcTemplate.update("INSERT INTO LIKES (FILM_ID, USER_ID) VALUES (?, ?)", filmId, userId);
    }

    public void deleteLike(int filmId, int userId) {
        int status = jdbcTemplate.update("DELETE FROM LIKES WHERE FILM_ID = ? AND USER_ID = ?", filmId, userId);
        if (status != 1) {
            throw new IllegalArgumentException("неправильный id");
        }
    }

    public List<Film> getTopFilms(int count) {
        String sql = ("SELECT F.FILM_ID, NAME, DESCRIPTION, RELEASE_DATE, DURATION, MPA_ID, " +
                "COUNT(USER_ID) AS COUNT FROM FILMS F LEFT JOIN LIKES L on F.FILM_ID = L.FILM_ID\n" +
                "GROUP BY F.FILM_ID ORDER BY COUNT DESC LIMIT " + count);
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Film f = new Film();
            f.setId(rs.getInt("FILM_ID"));
            f.setName(rs.getString("NAME"));
            f.setDescription(rs.getString("DESCRIPTION"));
            f.setReleaseDate(LocalDate.parse(rs.getString("RELEASE_DATE")));
            f.setDuration(rs.getInt("DURATION"));
            f.setMpa(mpaStorage.getMpaById(rs.getInt("MPA_ID")));
            f.setGenres(genresStorage.getGenresByFilmId(f.getId()));
            f.setRating(rs.getInt("COUNT"));
            return f;
        });
    }

    private boolean checkUserId(int userId) {
        Boolean checkUser = jdbcTemplate.queryForObject("SELECT EXISTS(SELECT * FROM USERS WHERE USER_ID = ?)",
                Boolean.class, userId);
        return Boolean.TRUE.equals(checkUser);
    }

    private boolean checkFilmId(int filmId) {
        Boolean checkFilm = jdbcTemplate.queryForObject("SELECT EXISTS(SELECT * FROM FILMS WHERE FILM_ID = ?)",
                Boolean.class, filmId);
        return Boolean.TRUE.equals(checkFilm);
    }

    public Integer countOfLikes(int filmId) {
        if (!checkFilmId(filmId)) throw new IllegalArgumentException("неправильный id");
        Integer countOfLikes = jdbcTemplate.queryForObject("SELECT COUNT(USER_ID) AS COUNT " +
                "FROM LIKES WHERE FILM_ID = ?", Integer.class, filmId);
        if (countOfLikes == null) {
            return 0;
        }
        return countOfLikes;
    }
}
