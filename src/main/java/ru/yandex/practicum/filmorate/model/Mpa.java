package ru.yandex.practicum.filmorate.model;

import lombok.*;

@Builder
@Setter
@Getter
@EqualsAndHashCode
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Mpa {
    private int id;
    private String name;
}
