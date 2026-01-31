package org.lgp.Validation;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.lgp.Service.BoardgameService;

@ApplicationScoped
public class BoardgameExistsValidator implements ConstraintValidator<BoardgameExists, String> {

    @Inject
    BoardgameService boardgameService;

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) return true;
        try {
            boardgameService.getBoardgame(value);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}