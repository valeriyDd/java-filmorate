package ru.yandex.practicum.filmorate.model;

import lombok.*;
import ru.yandex.practicum.filmorate.model.validator.ReleaseDate;

import javax.validation.constraints.*;
import java.time.LocalDate;
import java.util.List;

@Builder
@Setter
@Getter
@EqualsAndHashCode
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Film {

    private Integer id;
    @NotBlank
    private String name;
    @Size(max = 200)
    @NotNull
    private String description;
    @ReleaseDate
    @NotNull
    private LocalDate releaseDate;
    @Positive
    @NotNull
    private Integer duration;
    private Integer rating;
    private Mpa mpa;
    private List<Genres> genres;
}
