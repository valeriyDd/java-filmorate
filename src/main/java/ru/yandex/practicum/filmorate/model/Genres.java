package ru.yandex.practicum.filmorate.model;

import lombok.*;

@Builder
@Setter
@Getter
@EqualsAndHashCode
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Genres {
    private int id;
    private String name;
}
