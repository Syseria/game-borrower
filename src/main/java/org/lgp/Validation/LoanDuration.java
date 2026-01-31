package org.lgp.Validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({ TYPE })
@Retention(RUNTIME)
@Constraint(validatedBy = LoanDurationValidator.class)
@Documented
public @interface LoanDuration {
    String message() default "Due date must be after the borrowing date";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}