package org.lgp.Validation;

import jakarta.inject.Inject;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.lgp.Entity.User;
import org.lgp.Service.UserService;

public class UserExistsValidator implements ConstraintValidator<UserExists, String> {
    @Inject
    UserService userService;
    @Override
    public boolean isValid(String id, ConstraintValidatorContext context) {
        if (id == null || id.isBlank()) return true;
        try {
            User.UserSearchCriteria criteria = User.UserSearchCriteria.builder()
                    .id(id)
                    .build();
            userService.searchUsers(criteria);
            return true;
        } catch (Exception e) { return false; }
    }
}
