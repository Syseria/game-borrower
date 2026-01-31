package org.lgp.Validation;

import jakarta.inject.Inject;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.lgp.Service.InventoryService;

public class InventoryItemExistsValidator implements ConstraintValidator<InventoryItemExists, String> {
    @Inject
    InventoryService inventoryService;
    @Override
    public boolean isValid(String id, ConstraintValidatorContext context) {
        if (id == null || id.isBlank()) return true;
        try { inventoryService.getItem(id); return true; } catch (Exception e) { return false; }
    }
}
