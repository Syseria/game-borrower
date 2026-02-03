package org.lgp.DTO;

import org.lgp.Entity.User;

import java.util.Set;

public record UserProfileResponseDTO(String uid, String email, String name, String lname, Set<User.Role> roles) {
}
