
package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FilmControllerTest {

    private final FilmDbStorage filmDbStorage;
    private static Validator validator;


    @BeforeEach
    void beforeEach() {
        ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.usingContext().getValidator();
    }

    @Test
    @DirtiesContext
    void createFilm() {
        Film film = Film.builder().name("name").description("description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(100).mpa(new Mpa(1, null)).build();
        assertEquals(film, filmDbStorage.addFilm(film));
        assertEquals(1, filmDbStorage.getFilms().size());
    }

    @Test
    @DirtiesContext
    void updateFilm() {
        Film film = Film.builder().name("name").description("description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(100).mpa(new Mpa(1, null)).build();
        assertEquals(film, filmDbStorage.addFilm(film));
        assertEquals(1, filmDbStorage.getFilms().size());
        String newName = "newName";
        String newDescription = "newDescription";
        film.setName(newName);
        film.setDescription(newDescription);
        Film updatedFilm = filmDbStorage.updateFilm(film);
        assertEquals(newName, updatedFilm.getName());
        assertEquals(newDescription, updatedFilm.getDescription());
    }

    @Test
    @DirtiesContext
    void getAllFilms() {
        int len = filmDbStorage.getFilms().size();
        Film film = Film.builder().name("name").description("description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(100).mpa(new Mpa(1, "G")).build();
        Film film2 = Film.builder().name("name2").description("description2")
                .releaseDate(LocalDate.of(2001, 2, 2))
                .duration(100).mpa(new Mpa(1, "G")).build();
        filmDbStorage.addFilm(film);
        List<Film> films = new ArrayList<>(filmDbStorage.getFilms());
        assertEquals(len + 1, films.size());
        filmDbStorage.addFilm(film2);
        films = new ArrayList<>(filmDbStorage.getFilms());
        assertEquals(len + 2, films.size());
    }

    @DirtiesContext
    @Test
    void createFilmWithWrongName() {
        Film film = Film.builder().name(null).description("description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(100).mpa(new Mpa(1, "G")).build();
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertEquals(1, violations.size(), "Name is empty");
        Film film2 = Film.builder().name("").description("description")
                .releaseDate(LocalDate.of(2000, 1, 1)).duration(100).build();
        Set<ConstraintViolation<Film>> violations1 = validator.validate(film2);
        assertEquals(1, violations1.size(), "Name is empty");
    }

    @Test
    @DirtiesContext
    void createFilmWithWrongReleaseDate() {
        Film film = Film.builder().name("name").description("description")
                .releaseDate(LocalDate.of(1895, 12, 27))
                .duration(100).mpa(new Mpa(1, "G")).build();
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertEquals(1, violations.size(), "Wrong date");
        film.setReleaseDate(null);
        Set<ConstraintViolation<Film>> violations1 = validator.validate(film);
        assertEquals(1, violations1.size(), "Wrong date");
        Film film2 = Film.builder().name("name").description("description")
                .releaseDate(LocalDate.of(1895, 12, 28)).duration(100)
                .mpa(new Mpa(1, "G")).build();
        assertEquals(film2, filmDbStorage.addFilm(film2));
    }

    @Test
    @DirtiesContext
    void createFilmWithWrongDescription() {
        // 201 chars
        String description = "description/description/description/description/description/description/description/" +
                "description/description/description/description/description/description/description/description/";
        Film film = Film.builder().name("name").description(description)
                .releaseDate(LocalDate.of(1895, 12, 27))
                .duration(100).mpa(new Mpa(1, "G")).build();
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertEquals(1, violations.size(), "Wrong description");
        film.setDescription(null);
        Set<ConstraintViolation<Film>> violations2 = validator.validate(film);
        assertEquals(2, violations2.size(), "description = null");
        // 200 chars
        String description2 = "description/description/description/description/description/description/description/" +
                "description/description/description/description/description/description/description/description";
        Film film2 = Film.builder().name("name").description(description2)
                .releaseDate(LocalDate.of(1895, 12, 28))
                .duration(100).mpa(new Mpa(1, "G")).build();
        assertEquals(film2, filmDbStorage.addFilm(film2));
    }

    @Test
    @DirtiesContext
    void createFilmWithWrongDuration() {
        Film film = Film.builder().name("name").description("description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(-1).mpa(new Mpa(1, "G")).build();
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertEquals(1, violations.size(), "Wrong duration");
        film.setDuration(null);
        Set<ConstraintViolation<Film>> violations2 = validator.validate(film);
        assertEquals(1, violations2.size(), "Duration = null");
        Film film2 = Film.builder().name("name").description("description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(0).mpa(new Mpa(1, "G")).build();
        assertEquals(film2, filmDbStorage.addFilm(film2));
    }

    @Test
    @DirtiesContext
    void updateFilmWithWrongId() {
        Film film = Film.builder().name("name").description("description")
                .releaseDate(LocalDate.of(2000, 1, 1)).duration(100)
                .mpa(new Mpa(1, "G")).build();
        assertEquals(film, filmDbStorage.addFilm(film));
        film.setId(999);
        assertThrows(InvalidDataAccessApiUsageException.class, () -> filmDbStorage.updateFilm(film));
    }

    @Test
    @DirtiesContext
    void getFilmById() {
        Film film = Film.builder().name("name").description("description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(100).mpa(new Mpa(1, "G")).genres(new ArrayList<>()).build();
        filmDbStorage.addFilm(film);
        assertEquals(film, filmDbStorage.getFilm(film.getId()));
    }

    @Test
    @DirtiesContext
    void getFilmWithWrongId() {
        assertThrows(InvalidDataAccessApiUsageException.class, () -> filmDbStorage.getFilm(12399));
    }
}
