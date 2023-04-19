package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genres;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.genres.GenresStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;

@Slf4j
@RestController
public class FilmController {
    private final FilmService filmService;
    MpaStorage mpaStorage;
    GenresStorage genresStorage;

    @Autowired
    public FilmController(FilmService filmService, MpaStorage mpaStorage, GenresStorage genresStorage) {
        this.genresStorage = genresStorage;
        this.mpaStorage = mpaStorage;
        this.filmService = filmService;
    }

    @GetMapping("/films")
    public Collection<Film> getAllFilms() {
        return filmService.findAllFilms();
    }

    @GetMapping("/films/{id}")
    public Film getFilm(@PathVariable int id) {
        return filmService.getFilm(id);
    }

    @GetMapping("/films/popular")
    public List<Film> getPopularFilms(@RequestParam(defaultValue = "10") String count) {
        return filmService.getTopFilms(Integer.parseInt(count));
    }

    @GetMapping("/mpa")
    public List<Mpa> getMPAs() {
        return mpaStorage.getMPAs();
    }

    @GetMapping("/mpa/{id}")
    public Mpa getMpa(@PathVariable int id) {
        return mpaStorage.getMpaById(id);
    }

    @GetMapping("/genres")
    public List<Genres> getGenres() {
        return genresStorage.getGenres();
    }

    @GetMapping("/genres/{id}")
    public Genres getGenre(@PathVariable int id) {
        return genresStorage.getGenreById(id);
    }


    @PostMapping(value = "/films")
    public Film createFilm(@RequestBody @Valid Film film) {
        log.info("Creating film {}", film);
        return filmService.createFilm(film);
    }

    @PutMapping(value = "/films")
    public Film updateFilm(@RequestBody @Valid Film film) {
        log.info("Updating film {}", film);
        return filmService.updateFilm(film);
    }

    @PutMapping(value = "/films/{id}/like/{userId}")
    public void like(@PathVariable int id, @PathVariable int userId) {
        filmService.addLike(id, userId);
    }

    @DeleteMapping(value = "/films/{id}/like/{userId}")
    public void deleteLike(@PathVariable int id, @PathVariable int userId) {
        filmService.deleteLike(id, userId);
    }


}
