package ru.yandex.practicum.filmorate.model;

import lombok.*;

@Builder
@Setter
@Getter
@EqualsAndHashCode
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class MPA {
    private int id;
    private String name;
}
