package com.example;

import com.mysql.cj.jdbc.MysqlDataSource;
import org.jboss.logging.Logger;
import org.keycloak.component.ComponentModel;
import org.keycloak.credential.CredentialInput;
import org.keycloak.credential.CredentialInputValidator;
import org.keycloak.models.*;
import org.keycloak.storage.StorageId;
import org.keycloak.storage.UserStorageProvider;
import org.keycloak.storage.adapter.AbstractUserAdapterFederatedStorage;
import org.keycloak.storage.user.UserLookupProvider;
//import org.keycloak.storage.StorageId;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.Random;
import java.util.stream.Stream;

public class UserFederationProvider implements UserStorageProvider, UserLookupProvider, CredentialInputValidator
//        , UserQueryProvider,  // if you want to query the user by admin console,you should implement this interface
//        UserCredentialManager // if you want to customize credential,you should implement this interface
{

    private final KeycloakSession session;
    private final ComponentModel model;
    private Connection connection;
    private final UserFederationConfig config;
    private final DataSource ds;
    private static final Logger logger = Logger.getLogger(UserFederationProvider.class);
    public UserFederationProvider(KeycloakSession session, ComponentModel model) {
        this.session = session;
        this.model = model;
        this.config = new UserFederationConfig(model);
        MysqlDataSource mds = new MysqlDataSource();
        mds.setUrl(config.getJdbcUrl());
        mds.setUser(config.getUsername());
        mds.setPassword(config.getPassword());
        this.ds = mds;
        initializeConnection();
    }

    private void initializeConnection() {
        String jdbcUrl = model.getConfig().getFirst("jdbcUrl");
        String username = model.getConfig().getFirst("dbUsername");
        String password = model.getConfig().getFirst("dbPassword");
        try {
            connection = DriverManager.getConnection(jdbcUrl, username, password);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to connect to MySQL database", e);
        }
    }

    @Override
    public UserModel getUserById( RealmModel realm,String id) {
        logger.info("come to the getUserById function");
        logger.info("id is ：" + id);
        if(id.contains("f:")){
            //we should map the id with "company_code + kaiin_code"
            String localId = StorageId.externalId(id);
            logger.info("localId is ：" + localId);
            return null;
        }else{
            String[] codeArry=id.split("_");

            UserRepresentation u =findUserByCode(codeArry[0],codeArry[1]);
            if(u !=null){
                return createKeycloakUser(realm,u);
            }
        }

        return null;
    }

//    // this function should be used by getUserById, but we don't use it for now
//    private UserRepresentation findUserByID(String id) {
//        String sql = String.format("SELECT  %s AS username, %s AS password , %s AS email , %s AS firstName, %s AS lastName FROM %s WHERE id=%s;",
//                config.getUsernameField(), config.getPasswordField(),config.getEmailField(),config.getFirstNameField(),config.getLastNameField(),
//                config.getUserTable(), id);
//        logger.info(sql);
//        try (Connection c = ds.getConnection();
//             PreparedStatement ps = c.prepareStatement(sql)) {
//            try (ResultSet rs = ps.executeQuery()) {
//                if (!rs.next()) return null;
//                UserRepresentation u = new UserRepresentation();
//                u.setId(rs.getString("id"));
//                u.setUsername(rs.getString("username"));
//                u.setPasswordHash(rs.getString("password"));
//                u.setEmail(rs.getString("email"));
//                u.setFirstName(rs.getString("firstName"));
//                u.setLastName(rs.getString("lastName"));
//                return u;
//            }
//        } catch (SQLException e) {
//            throw new RuntimeException(e);
//        }
//    }
    private UserRepresentation findUserByCode(String companyCode,String kaiinCode) {
        String sql = String.format("SELECT id, %s AS username, %s AS password , %s AS email , %s AS firstName, %s AS lastName FROM %s WHERE company_code = '%s' or kaiin_code = '%s'",
                config.getUsernameField(), config.getPasswordField(),config.getEmailField(),config.getFirstNameField(),config.getLastNameField(),
                config.getUserTable(), companyCode,kaiinCode);
        logger.info("findUserByCode SQL:" + sql);

        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                UserRepresentation u = new UserRepresentation();
                u.setId(rs.getString("id"));
                u.setUsername(rs.getString("username"));
                u.setPasswordHash(rs.getString("password"));
                u.setEmail(rs.getString("email"));
                u.setFirstName(rs.getString("firstName"));
                u.setLastName(rs.getString("lastName"));
                return u;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public UserModel getUserByUsername( RealmModel realm,String username) {
        logger.info("come to getUserByUsername function");
        logger.info("username is：" + username);
        UserRepresentation u=new UserRepresentation();
        if(!username.contains("_")){
            u = findUser(username);
        }
        else {
            String[] codeArry=username.split("_");
            u =findUserByCode(codeArry[0],codeArry[1]);
        }
        if(u !=null){
            return createKeycloakUser(realm,u);
        }
        return null;
    }

    private UserRepresentation findUser(String value) {
        String sql = String.format("SELECT id, %s AS username, %s AS password , %s AS email , %s AS firstName, %s AS lastName FROM %s WHERE username = '%s' or email = '%s'",
                config.getUsernameField(), config.getPasswordField(),config.getEmailField(),config.getFirstNameField(),config.getLastNameField(),
                config.getUserTable(), value,value);
        logger.info(sql);

        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                UserRepresentation u = new UserRepresentation();
                u.setId(rs.getString("id"));
                u.setUsername(rs.getString("username"));
                u.setPasswordHash(rs.getString("password"));
                u.setEmail(rs.getString("email"));
                u.setFirstName(rs.getString("firstName"));
                u.setLastName(rs.getString("lastName"));
                return u;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public UserModel getUserByEmail(RealmModel realm,String email) {
        logger.info("come to getUserByEmail function");
        logger.info("email is：" + email);
        UserRepresentation u =findUser(email);
        if(u !=null){
            return createKeycloakUser(realm,u);
        }
        return null;
    }

    private UserModel createKeycloakUser(RealmModel realm, UserRepresentation u) {
        logger.info("createKeycloakUser,MySQLUserRepresentation:" + u.getPasswordHash());
        UserModel adapter = new AbstractUserAdapterFederatedStorage(session, realm, model) {

            @Override
            public String getUsername() { return u.getUsername(); }

            @Override
            public void setUsername(String s) {
                u.setUsername(s);
            }

            @Override
            public String getEmail() {
                return u.getEmail();
            }

            @Override
            public void setEmail(String s) {
                u.setEmail(s);
            }

            @Override
            public String getFirstName() {
                return u.getFirstName();
            }

            @Override
            public void setFirstName(String s) {
                u.setFirstName(s);
            }

            @Override
            public String getLastName() {
                return u.getLastName();
            }

            @Override
            public void setLastName(String s) {
                u.setLastName(s);
            }

            @Override
            public Stream<GroupModel> getGroupsStream(String search, Integer first, Integer max) {
                return super.getGroupsStream(search, first, max);
            }

            @Override
            public long getGroupsCount() {
                return super.getGroupsCount();
            }

            @Override
            public long getGroupsCountByNameContaining(String search) {
                return super.getGroupsCountByNameContaining(search);
            }

            @Override
            public boolean isFederated() {
                logger.info("super.isFederated():" + super.isFederated());
                return super.isFederated();
            }

            @Override
            public String getId() {
                return StorageId.keycloakId(model, u.getId());
            }
        };
        logger.info("info:" + u.getEmail() + " ," +u.getFirstName()+u.getLastName());

        adapter.setEnabled(true);
        adapter.setEmail(u.getEmail());
        adapter.setFirstName(u.getFirstName());
        adapter.setLastName(u.getLastName());
        return adapter;
    }

    @Override
    public boolean isValid(RealmModel realm, UserModel user, CredentialInput input) {
        // for this case,we don't need this function to identify the password,so just return true
        logger.info("isValid**********************************************");
        if (!supportsCredentialType(input.getType()) || !(input instanceof UserCredentialModel)) {
            return false;
        }else{
            return true;
        }

    }

    @Override
    public boolean supportsCredentialType(String credentialType) {
        return org.keycloak.models.credential.PasswordCredentialModel.TYPE.equals(credentialType);
    }

    @Override
    public boolean isConfiguredFor(RealmModel realm, UserModel user, String credentialType) {
        return supportsCredentialType(credentialType);
    }

    @Override
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to close database connection", e);
        }
    }
}