package org.lgp.DTO;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import org.lgp.Validation.InventoryItemExists;
import org.lgp.Validation.LoanDuration;
import org.lgp.Validation.UserExists;

import java.util.Date;

@LoanDuration
public record CreateLoanRequestDTO(
        @NotNull(message = "Inventory Item ID is required")
        @InventoryItemExists
        String inventoryItemId,

        @NotNull(message = "User ID is required")
        @UserExists
        String userId,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        Date dueDate
) {
}
