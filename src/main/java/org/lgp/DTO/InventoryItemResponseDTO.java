package org.lgp.DTO;

import org.lgp.Entity.InventoryItem;

public record InventoryItemResponseDTO(
        String id,
        String boardgameId,
        String boardgameTitle,
        InventoryItem.Status status,
        InventoryItem.Condition condition,
        String details,
        String currentLoanId
) {
}
