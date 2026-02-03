package org.lgp.DTO;

import jakarta.validation.constraints.NotBlank;
import org.lgp.Entity.InventoryItem;
import org.lgp.Validation.BoardgameExists;
import org.lgp.Validation.ValidEnum;

public record InventoryItemRequestDTO(
        @NotBlank
        @BoardgameExists
        String boardgameId,

        @NotBlank
        @ValidEnum(enumClass = InventoryItem.Condition.class)
        String condition,

        String details
) {
}
