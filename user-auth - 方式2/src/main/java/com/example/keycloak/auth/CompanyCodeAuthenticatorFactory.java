package com.example.keycloak.auth;

import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.AuthenticationExecutionModel.Requirement;
import org.keycloak.provider.ProviderConfigProperty;

import java.util.List;

public class CompanyCodeAuthenticatorFactory implements AuthenticatorFactory {

    public static final String ID = "company-code-authenticator";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getDisplayType() {
        return "Company Code Validator New";
    }

    @Override
    public String getHelpText() {
        return "Extracts company_code from login form and stores in session";
    }

    @Override
    public Authenticator create(KeycloakSession session) {
        return new CompanyCodeAuthenticator(session);
    }

    @Override
    public boolean isConfigurable() {
        return false;
    }

    @Override
    public Requirement[] getRequirementChoices() {
        return new Requirement[] { Requirement.REQUIRED };
    }

    @Override
    public void init(org.keycloak.Config.Scope config) {
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
    }

    @Override
    public void close() {
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return List.of();
    }

    // ✅ Keycloak 17+ 需要实现的方法：
    @Override
    public String getReferenceCategory() {
        return null; // 可返回 "company_code" 或 null
    }

    @Override
    public boolean isUserSetupAllowed() {
        return false;
    }
}