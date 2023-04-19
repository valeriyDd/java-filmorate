package ru.yandex.practicum.filmorate.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;

import java.net.BindException;
import java.sql.SQLException;
import java.util.Map;

@RestControllerAdvice
class ErrorResponse {
    @ExceptionHandler({IllegalArgumentException.class, SQLException.class, DataIntegrityViolationException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleWrongArgument(final RuntimeException e) {
        return Map.of("неправильный аргумент", e.getMessage());
    }

    @ExceptionHandler({BindException.class, WebExchangeBindException.class, ValidationException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleValidate(final RuntimeException e) {
        return Map.of("ошибка валидации", e.getMessage());
    }
}