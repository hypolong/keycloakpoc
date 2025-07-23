package com.example;

import org.keycloak.component.ComponentModel;

public class UserFederationConfig {
    private final ComponentModel model;

    public UserFederationConfig(ComponentModel model) {
        this.model = model;
    }

    public String getJdbcUrl() { return model.get("jdbcUrl"); }
    public String getUsername() { return model.get("dbUsername"); }
    public String getPassword() { return model.get("dbPassword"); }
    public String getUserTable() { return model.get("userTable", "users"); }
    public String getUsernameField() { return model.get("usernameField", "username"); }
    public String getPasswordField() { return model.get("passwordField", "password"); }
    public String getEmailField() { return model.get("emailField", "email"); }
    public String getFirstNameField() { return model.get("firstNameField", "firstName"); }
    public String getLastNameField() { return model.get("lastNameField", "lastName"); }
}
