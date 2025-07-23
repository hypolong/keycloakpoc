package com.example;

import org.keycloak.component.ComponentModel;
//import org.keycloak.credential.CredentialInput;
import org.keycloak.models.KeycloakSession;
//import org.keycloak.models.credential.PasswordCredentialModel;
import org.keycloak.provider.ProviderConfigProperty;
//import org.keycloak.provider.ProviderConfigurationBuilder;
import org.keycloak.storage.UserStorageProviderFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserFederationProviderFactory implements UserStorageProviderFactory<UserFederationProvider> {

    public static final String PROVIDER_ID = "mysql-user-federation";

    @Override
    public UserFederationProvider create(KeycloakSession session, ComponentModel model) {
        return new UserFederationProvider(session, model);
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        ProviderConfigProperty jdbcUrl = new ProviderConfigProperty(
                "jdbcUrl", "JDBC URL", "jdbc:mysql://localhost:3306/mochikabu?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC", ProviderConfigProperty.STRING_TYPE, null);
        ProviderConfigProperty dbUser = new ProviderConfigProperty(
                "dbUsername", "DB Username", "DB Username", ProviderConfigProperty.STRING_TYPE, null);
        ProviderConfigProperty dbPass = new ProviderConfigProperty(
                "dbPassword", "DB Password", "DB Username", ProviderConfigProperty.PASSWORD, null);
        ProviderConfigProperty userTable = new ProviderConfigProperty(
                "userTable", "User Table Name", "User Table Name", ProviderConfigProperty.STRING_TYPE, "users");
        ProviderConfigProperty usernameField = new ProviderConfigProperty(
                "usernameField", "Username Field", "Username Field", ProviderConfigProperty.STRING_TYPE, "username");
        ProviderConfigProperty passwordField = new ProviderConfigProperty(
                "passwordField", "Password Field", "Password Field", ProviderConfigProperty.STRING_TYPE, "password");
        ProviderConfigProperty emailField = new ProviderConfigProperty(
                "emailField", "email Field", "email Field", ProviderConfigProperty.STRING_TYPE, "email");
        ProviderConfigProperty firstNameField = new ProviderConfigProperty(
                "firstNameField", "firstName Field", "firstName Field", ProviderConfigProperty.STRING_TYPE, "firstname");
        ProviderConfigProperty lastNameField = new ProviderConfigProperty(
                "lastNameField", "lastName Field", "lastName Field", ProviderConfigProperty.STRING_TYPE, "lastname");
        return Arrays.asList(jdbcUrl, dbUser, dbPass, userTable, usernameField, passwordField,emailField,firstNameField,lastNameField);
    }

    @Override
    public String getHelpText() {
        return "MySQL User Federation with Password Support";
    }

    @Override
    public Map<String, Object> getTypeMetadata() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("passwordSupported", true);  // 关键声明
        return metadata;
    }

//    public List<Class<? extends CredentialInput>> getSupportedCredentialTypes() {
//        return List.of(PasswordCredentialModel.class);
//    }
}