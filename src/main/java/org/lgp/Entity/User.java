package org.lgp.Entity;

import com.google.cloud.firestore.annotation.DocumentId;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class User {

    @DocumentId
    private String uid;
    private String name;
    private String lname;
    private String role;

    public record RegisterRequestDTO(
            String email,
            String password,
            String name,
            String lname
    ) {}

    public User() {
    }

    public User(String uid, String name, String lname, String role) {
        this.uid = uid;
        this.name = name;
        this.lname = lname;
        this.role = role;
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

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return "User{" +
                "uid='" + uid + '\'' +
                ", name='" + name + '\'' +
                ", lname='" + lname + '\'' +
                ", role='" + role + '\'' +
                '}';
    }
}
