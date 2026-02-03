package org.lgp.DTO;

import jakarta.validation.constraints.NotBlank;
import org.lgp.Entity.InventoryItem;
import org.lgp.Validation.ValidEnum;

public record InventoryStatusUpdateRequestDTO(
        @NotBlank
        @ValidEnum(enumClass = InventoryItem.Status.class)
        String status,

        @ValidEnum(enumClass = InventoryItem.Condition.class)
        String condition,

        String details
) {
}
