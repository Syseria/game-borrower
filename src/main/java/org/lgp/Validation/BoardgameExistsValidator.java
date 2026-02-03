package org.lgp.Validation;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.lgp.DTO.BoardgameSearchCriteria;
import org.lgp.Service.BoardgameService;

@ApplicationScoped
public class BoardgameExistsValidator implements ConstraintValidator<BoardgameExists, String> {

    @Inject
    BoardgameService boardgameService;

    @Override
    public boolean isValid(String id, ConstraintValidatorContext context) {
        if (id == null || id.isBlank()) return true;
        try {
            BoardgameSearchCriteria criteria = BoardgameSearchCriteria.builder()
                    .id(id)
                    .build();
            boardgameService.searchBoardgames(criteria);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}