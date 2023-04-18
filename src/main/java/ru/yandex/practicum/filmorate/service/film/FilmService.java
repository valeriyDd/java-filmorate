package ru.yandex.practicum.filmorate.service.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;
import ru.yandex.practicum.filmorate.storage.like.LikesStorage;

import javax.validation.ValidationException;
import java.time.LocalDate;
import java.time.Month;
import java.util.HashSet;
import java.util.List;

@Service
@Slf4j
public class FilmService {

    private final FilmStorage filmStorage;
    private final LikesStorage likesStorage;
    private final GenreStorage genreStorage;
    private static final LocalDate FILM_REALISE_DATE = LocalDate.of(1895, Month.DECEMBER, 28);

    @Autowired
    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage,
                       LikesStorage likesStorage, GenreStorage genreStorage) {
        this.filmStorage = filmStorage;
        this.likesStorage = likesStorage;
        this.genreStorage = genreStorage;
    }

    public Film createFilm(Film film) {
        validate(film);
        log.info("Фильм '{}' успешно прошел валидацию", film.getName());
        filmStorage.add(film);
        return film;
    }

    public Film updateFilm(Film film) {
        filmStorage.update(film);
        genreStorage.updateGenresOfFilm(film);
        Film filmReturn = filmStorage.getFilm(film.getId()).orElseThrow(
                () -> new NotFoundException(String.format("Фильм с id %d не найден", film.getId())));
        if (film.getGenres() == null) {
            filmReturn.setGenres(null);
        } else if (film.getGenres().isEmpty()) {
            filmReturn.setGenres(new HashSet<>());
        }
        log.info("Фильм с id {} обновлен", film.getId());
        return filmReturn;
    }

    public List<Film> getFilms() {
        log.info("Фильмы получены");
        return filmStorage.findAllFilms();
    }

    public Film getFilmById(int id) {
        return filmStorage.getFilm(id)
                .orElseThrow(() -> new NotFoundException(String.format("Фильм с id %d не найден", id)));
    }

    public void putALike(int id, int userId) {
        likesStorage.addLike(id, userId);
        log.info("Лайк для c id '{}' успешно добавлен", id);
    }

    public void removeLike(int id, int userId) {
        likesStorage.removeLike(id, userId);
        log.info("Лайк для c id '{}' успешно удален", id);
    }

    public List<Film> getRating(int count) {
        log.info("Список рейтинга полечен");
        return likesStorage.getPopular(count);
    }

    private static void validate(Film film) {
        if (film.getReleaseDate().isBefore(FILM_REALISE_DATE)) {
            log.info("Дата фильма должна быть не позже 28.12.1895");
            throw new ValidationException("Дата фильма должна быть не позже 28.12.1895");
        }
    }


}
