package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import ru.yandex.practicum.filmorate.validator.ReleaseDate;

import javax.validation.constraints.*;
import java.time.LocalDate;
import java.util.List;

@Data
public class Film {
    private Integer id;
    @NotBlank
    @NotNull
    private String name;
    @Size(max = 200)
    private String description;
    @Positive
    private Integer duration;
    private List<Genre> genres;
    private Mpa mpa;
    @ReleaseDate
    private LocalDate releaseDate;

    public Film(Integer id, String name, String description, Integer duration, List<Genre> genres, Mpa mpa,
                LocalDate releaseDate) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.duration = duration;
        this.genres = genres;
        this.mpa = mpa;
        this.releaseDate = releaseDate;
    }
}
