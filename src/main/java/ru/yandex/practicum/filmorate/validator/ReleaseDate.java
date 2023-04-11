package ru.yandex.practicum.filmorate.validator;
import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;

@Target({ FIELD, PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = ValidatorReleaseDate.class)
public @interface ReleaseDate {
    public String message() default "Invalid releaseDate: Дата не должна раньше 28.12.1895";
    public Class<?>[] groups() default {};
    public Class<? extends Payload>[] payload() default {};
}