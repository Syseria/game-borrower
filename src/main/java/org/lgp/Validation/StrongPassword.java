package org.lgp.Validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Pattern;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[^a-zA-Z0-9]).*$", message = "Weak password") // We reuse Pattern here!
@Target({ FIELD })
@Retention(RUNTIME)
@Constraint(validatedBy = {}) // No Validator class needed since we are composing existing annotations
public @interface StrongPassword {
    String message() default "Password must contain uppercase, lowercase, and special characters";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}