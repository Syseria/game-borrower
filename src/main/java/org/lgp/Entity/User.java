package org.lgp.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.cloud.firestore.annotation.DocumentId;

import com.google.cloud.firestore.annotation.Exclude;
import com.google.firebase.database.annotations.NotNull;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.lgp.Validation.StrongPassword;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@RegisterForReflection
public class User {

    @DocumentId
    private String uid;
    private String name;
    private String lname;
    private String email;
    private Set<String> roles = new HashSet<>();

    @RegisterForReflection
    public enum Role {
        USER("user"),
        MAINTAINER("maintainer"),
        ADMIN("admin");

        private final String value;

        Role(String value) {
            this.value = value;
        }

        // This annotation ensures Firestore/Jackson uses this value when saving
        @com.fasterxml.jackson.annotation.JsonValue
        public String getValue() {
            return value;
        }

        public static Role fromString(String text) {
            for (Role b : Role.values()) {
                if (b.value.equalsIgnoreCase(text)) {
                    return b;
                }
            }
            return null;
        }
    }

    public record UserProfileResponseDTO(
            String uid,
            String email,
            String name,
            String lname,
            Set<String> roles
    ) {}

    public record RegisterRequestDTO(
            @NotBlank(message = "Email can't be empty")
            @Email(message = "Invalid email format")
            String email,

            @NotBlank(message = "Password can't be empty")
            @Size(min = 8, message = "Password must be at least 8 characters long")
            @StrongPassword
            String password,

            @NotBlank(message = "First name can't be empty")
            String name,
            @NotBlank(message = "Last name can't be empty")
            String lname
    ) {}

    public record UpdateProfileRequestDTO(
            String name,
            String lname
    ) {}

    public record UpdateEmailRequestDTO(
            @NotBlank(message = "Email is required")
            @Email(message = "Invalid email format")
            String email
    ) {}

    public record UpdatePasswordRequestDTO(
            @NotBlank(message = "Password is required")
            @Size(min = 8, message = "Password must be at least 8 characters")
            @StrongPassword
            String password
    ) {}

    public User() {
        this.roles.add(Role.USER.getValue());
    }

    public User(String uid, String name, String lname, String email) {
        this();
        this.uid = uid;
        this.name = name;
        this.lname = lname;
        this.email = email;
    }

    @Exclude
    @JsonIgnore
    public Set<Role> getEnumRoles() {
        return roles.stream()
                .map(Role::fromString)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    public void addRole(Role role) {
        this.roles.add(role.getValue());
    }

    public void removeRole(Role role) {
        this.roles.remove(role.getValue());
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLname() {
        return lname;
    }

    public void setLname(String lname) {
        this.lname = lname;
    }

    public String getEmail() { return email; }

    public void setEmail(String email) { this.email = email; }

    public Set<String> getRoles() { return roles; }

    public void setRoles(Set<String> roles) { this.roles = roles; }

    @Override
    public String toString() {
        return "User{" +
                "uid='" + uid + '\'' +
                ", name='" + name + '\'' +
                ", lname='" + lname + '\'' +
                ", email='" + email + '\'' +
                ", roles=" + roles +
                '}';
    }
}
