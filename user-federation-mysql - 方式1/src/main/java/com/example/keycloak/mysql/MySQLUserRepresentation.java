package com.example.keycloak.mysql;

public class MySQLUserRepresentation {
    private String id;
    private String username;
    private String passwordHash;

    private String email;

    private String firstName;

    private String lastName;

    public MySQLUserRepresentation() {
    }

    public MySQLUserRepresentation(String id, String username, String passwordHash,String email,String fistName,String lastName) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.email=email;
        this.firstName=fistName;
        this.lastName=lastName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String s) {
        this.email = s;
    }

    public String getFirstname() {
        return firstName;
    }
    public void setFirstName(String s) {
        this.firstName = s;
    }

    public String getLastName() {
        return lastName;
    }
    public void setLastname(String s) {
        this.lastName = s;
    }

    @Override
    public String toString() {
        return "MySQLUserRepresentation{" +
                "id='" + id + '\'' +
                ", username='" + username + '\'' +
                ", passwordHash='" + passwordHash + '\'' +
                ", email='" + email + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                '}';
    }
}
