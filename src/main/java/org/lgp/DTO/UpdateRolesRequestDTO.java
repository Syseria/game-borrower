package org.lgp.DTO;

import jakarta.validation.constraints.NotNull;
import org.lgp.Entity.User;

import java.util.Set;

public record UpdateRolesRequestDTO(@NotNull(message = "Roles list cannot be null") Set<User.Role> roles) {
}
