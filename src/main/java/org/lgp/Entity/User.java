package org.lgp.Entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.cloud.firestore.annotation.DocumentId;
import com.google.cloud.firestore.annotation.Exclude;
import com.google.cloud.firestore.annotation.PropertyName;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

    private Set<Role> roles = new HashSet<>();

    @RegisterForReflection
    public enum Role {
        USER("user"),
        MAINTAINER("maintainer"),
        ADMIN("admin");

        private final String value;

        Role(String value) {
            this.value = value;
        }

        @JsonValue
        public String getValue() {
            return value;
        }

        @JsonCreator
        public static Role fromString(String text) {
            if (text == null) return null;
            for (Role b : Role.values()) {
                if (b.value.equalsIgnoreCase(text)) {
                    return b;
                }
            }
            return null;
        }
    }

    // -------------------------------------------------------------
    // CONSTRUCTORS
    // -------------------------------------------------------------

    public User() {
        this.roles.add(Role.USER);
    }

    public User(String uid, String name, String lname, String email) {
        this();
        this.uid = uid;
        this.name = name;
        this.lname = lname;
        this.email = email;
    }

    // -------------------------------------------------------------
    // LOGIC & API ACCESSORS
    // -------------------------------------------------------------

    // @Exclude prevents Firestore from using this (it would save "USER" otherwise)
    // Jackson will still use this for API JSON automatically
    @Exclude
    public Set<Role> getRoles() {
        return roles;
    }

    @Exclude
    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    @Exclude
    public void addRole(Role role) {
        this.roles.add(role);
    }

    @Exclude
    public void removeRole(Role role) {
        this.roles.remove(role);
    }

    // -------------------------------------------------------------
    // FIRESTORE "SHADOW" ACCESSORS
    // -------------------------------------------------------------
    @JsonIgnore
    @PropertyName("roles")
    public Set<String> getRolesDb() {
        if (this.roles == null) return new HashSet<>();
        return this.roles.stream()
                .map(Role::getValue)
                .collect(Collectors.toSet());
    }

    @JsonIgnore
    @PropertyName("roles")
    public void setRolesDb(Set<String> roleStrings) {
        if (roleStrings == null) {
            this.roles = new HashSet<>();
        } else {
            this.roles = roleStrings.stream()
                    .map(Role::fromString)
                    .filter(Objects::nonNull) // Prevent nulls if DB has invalid strings
                    .collect(Collectors.toSet());
        }
    }

    // -------------------------------------------------------------
    // STANDARD GETTERS/SETTERS
    // -------------------------------------------------------------

    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLname() { return lname; }
    public void setLname(String lname) { this.lname = lname; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

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

    // -------------------------------------------------------------
    // DTOs
    // -------------------------------------------------------------

    public record UserProfileResponseDTO(
            String uid,
            String email,
            String name,
            String lname,
            Set<Role> roles
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

    public record UpdateRolesRequestDTO(
            @NotNull(message = "Roles list cannot be null")
            Set<Role> roles
    ) {}
}