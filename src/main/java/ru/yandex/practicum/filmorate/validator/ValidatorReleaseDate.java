package ru.yandex.practicum.filmorate.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.LocalDate;

public class ValidatorReleaseDate implements ConstraintValidator<ReleaseDate, LocalDate> {
    private static final LocalDate birthdayOfFilms = LocalDate.of(1895, 12, 28);

    @Override
    public boolean isValid(LocalDate releaseDate, ConstraintValidatorContext cxt) {
        return !releaseDate.isBefore(birthdayOfFilms);
    }
}