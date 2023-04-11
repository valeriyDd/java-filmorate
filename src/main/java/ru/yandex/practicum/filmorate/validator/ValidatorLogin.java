package ru.yandex.practicum.filmorate.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class ValidatorLogin implements ConstraintValidator<Login, String> {

    @Override
    public boolean isValid(String login, ConstraintValidatorContext cxt) {
        return !(login == null || login.contains(" ") || login.isBlank());
    }
}