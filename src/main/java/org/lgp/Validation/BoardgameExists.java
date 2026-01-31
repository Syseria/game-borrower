package org.lgp.Validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({ FIELD, PARAMETER })
@Retention(RUNTIME)
@Constraint(validatedBy = BoardgameExistsValidator.class)
@Documented
public @interface BoardgameExists {
    String message() default "The specified board game does not exist";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}