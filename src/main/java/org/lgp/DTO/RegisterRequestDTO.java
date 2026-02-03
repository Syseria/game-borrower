package org.lgp.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.lgp.Validation.StrongPassword;

public record RegisterRequestDTO(
        @NotBlank(message = "Email can't be empty")
        @Email(message = "Invalid email format")
        String email,

        @NotBlank(message = "Password can't be empty")
        @Size(min = 8, message = "Password must be at least 8 characters long")
        @StrongPassword String password,

        @NotBlank(message = "First name can't be empty")
        String name,

        @NotBlank(message = "Last name can't be empty")
        String lname
) {
}
