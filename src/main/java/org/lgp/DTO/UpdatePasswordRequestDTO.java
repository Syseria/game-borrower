package org.lgp.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.lgp.Validation.StrongPassword;

public record UpdatePasswordRequestDTO(
        @NotBlank(message = "Password is required") @Size(min = 8, message = "Password must be at least 8 characters") @StrongPassword String password) {
}
