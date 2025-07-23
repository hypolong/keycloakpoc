package com.example.keycloak.auth;

public class UserRepresentation {
    private String id;
    private String username;
    private String password;

    private String email;

    private String firstName;

    private String lastName;

    private String companyCode;

    private String memberCode;
    public UserRepresentation() {
    }

    public UserRepresentation(String id, String username, String password, String email, String fistName, String lastName,String companyCode,String memberCode) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email=email;
        this.firstName=fistName;
        this.lastName=lastName;
        this.companyCode=companyCode;
        this.memberCode=memberCode;
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

    public void setUsername(String s) {
        this.username = s;
    }

    public String getPasswordHash() {
        return password;
    }

    public void setPasswordHash(String s) {
        this.password = s;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String s) {
        this.email = s;
    }

    public String getFirstName() {
        return firstName;
    }
    public void setFirstName(String s) {
        this.firstName = s;
    }

    public String getLastName() {
        return lastName;
    }
    public void setLastName(String s) {
        this.lastName = s;
    }

    public String getCompanyCode() {
        return companyCode;
    }
    public void setCompanyCode(String s) {
        this.companyCode = s;
    }

    public String getMemberCode() {
        return memberCode;
    }
    public void setMemberCode(String s) {
        this.memberCode = s;
    }



    @Override
    public String toString() {
        return "MySQLUserRepresentation{" +
                "id='" + id + '\'' +
                ", username='" + username + '\'' +
                ", passwordHash='" + password + '\'' +
                ", email='" + email + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                '}';
    }
}
