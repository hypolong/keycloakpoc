package com.example.keycloak.mysql;

import org.keycloak.Config;
import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.storage.UserStorageProviderFactory;

////other interfaces you may want to use
//import org.keycloak.storage.UserStorageProvider;
//import org.keycloak.storage.UserStorageProviderModel;
//import org.keycloak.models.RealmModel;

import java.util.*;

public class MySQLUserFederationProviderFactory
        implements UserStorageProviderFactory<MySQLUserFederationProvider> {

    public static final String PROVIDER_ID = "standard-user-federation";

    @Override
    public MySQLUserFederationProvider create(KeycloakSession session, ComponentModel model) {
        return new MySQLUserFederationProvider(session, model);
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public String getHelpText() {
        return "Standard Connect to external MySQL to provide user federation.";
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        ProviderConfigProperty jdbcUrl = new ProviderConfigProperty(
                "jdbcUrl", "JDBC URL", "jdbc:mysql://localhost:3306/mochikabu?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC", ProviderConfigProperty.STRING_TYPE, null);
        ProviderConfigProperty dbUser = new ProviderConfigProperty(
                "dbUsername", "DB Username", null, ProviderConfigProperty.STRING_TYPE, null);
        ProviderConfigProperty dbPass = new ProviderConfigProperty(
                "dbPassword", "DB Password", null, ProviderConfigProperty.PASSWORD, null);
        ProviderConfigProperty userTable = new ProviderConfigProperty(
                "userTable", "User Table Name", null, ProviderConfigProperty.STRING_TYPE, "users");
        ProviderConfigProperty usernameField = new ProviderConfigProperty(
                "usernameField", "Username Field", null, ProviderConfigProperty.STRING_TYPE, "username");
        ProviderConfigProperty passwordField = new ProviderConfigProperty(
                "passwordField", "Password Field", null, ProviderConfigProperty.STRING_TYPE, "password");
        ProviderConfigProperty emailField = new ProviderConfigProperty(
                "emailField", "email Field", null, ProviderConfigProperty.STRING_TYPE, "email");
        ProviderConfigProperty firstNameField = new ProviderConfigProperty(
                "firstNameField", "firstName Field", null, ProviderConfigProperty.STRING_TYPE, "firstname");
        ProviderConfigProperty lastNameField = new ProviderConfigProperty(
                "lastNameField", "lastName Field", null, ProviderConfigProperty.STRING_TYPE, "lastname");
        return Arrays.asList(jdbcUrl, dbUser, dbPass, userTable, usernameField, passwordField,emailField,firstNameField,lastNameField);
    }

    @Override
    public void init(Config.Scope config) { }

    @Override
    public void postInit(KeycloakSessionFactory factory) { }

    @Override
    public void close() { }
}
