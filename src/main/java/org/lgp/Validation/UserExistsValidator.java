package org.lgp.Validation;

import jakarta.inject.Inject;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.lgp.Service.UserService;

public class UserExistsValidator implements ConstraintValidator<UserExists, String> {
    @Inject
    UserService userService;
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) return true;
        try { userService.getUser(value); return true; } catch (Exception e) { return false; }
    }
}
