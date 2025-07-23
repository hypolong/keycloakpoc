package com.example.keycloak.mysql;

import org.jboss.logging.Logger;
import org.keycloak.component.ComponentModel;
import org.keycloak.credential.CredentialInput;
import org.keycloak.credential.CredentialInputValidator;
import org.keycloak.models.*;
import org.keycloak.models.credential.PasswordCredentialModel;
import org.keycloak.storage.StorageId;
import org.keycloak.storage.adapter.AbstractUserAdapterFederatedStorage;
import org.keycloak.storage.UserStorageProvider;
import org.keycloak.storage.user.UserLookupProvider;

import javax.sql.DataSource;
import com.mysql.cj.jdbc.MysqlDataSource;

import java.sql.*;
import java.util.stream.Stream;

public class MySQLUserFederationProvider implements UserStorageProvider,
        UserLookupProvider, CredentialInputValidator {

    private final KeycloakSession session;
    private final ComponentModel model;
    private final MySQLUserFederationConfig config;
    private final DataSource ds;

    private static final Logger logger = Logger.getLogger(MySQLUserFederationProvider.class);
    public MySQLUserFederationProvider(KeycloakSession session,
                                       ComponentModel model) {
        this.session = session;
        this.model = model;
        this.config = new MySQLUserFederationConfig(model);
        MysqlDataSource mds = new MysqlDataSource();
        mds.setUrl(config.getJdbcUrl());
        mds.setUser(config.getUsername());
        mds.setPassword(config.getPassword());
        this.ds = mds;
    }

    @Override
    public void close() { }

    @Override
    public UserModel getUserByUsername(RealmModel realm,String username) {
        logger.info("getUserByUsername");
        MySQLUserRepresentation u =null;
        if(username.contains(",")) {

            String[] codeArry = username.split(",");
            u = findUserByCode(codeArry[0], codeArry[1]);

        }else {
            u = findUser(username);
        }
        if (u == null) return null;
        return createKeycloakUser(realm, u);
    }

    @Override
    public CredentialValidationOutput getUserByCredential(RealmModel realm, CredentialInput input) {
        logger.info("CredentialValidationOutput");
        return UserLookupProvider.super.getUserByCredential(realm, input);
    }

    @Override
    public UserModel getUserByEmail(RealmModel realm, String s) {
        logger.info("getUserByEmail");
        MySQLUserRepresentation u = findUser( s);
        if (u == null) return null;
        return createKeycloakUser(realm, u);
    }

    @Override
    public UserModel getUserById(RealmModel realm,String id) {
        logger.info("getUserById");
        MySQLUserRepresentation u = findUser(id);
        if (u == null) return null;
        return createKeycloakUser(realm, u);
    }

    private MySQLUserRepresentation findUser(String value) {
        String sql = String.format("SELECT id, %s AS username, %s AS password , %s AS email , %s AS firstName, %s AS lastName FROM %s WHERE username = '%s' or email = '%s'",
                config.getUsernameField(), config.getPasswordField(),config.getEmailField(),config.getFirstNameField(),config.getLastNameField(),
                config.getUserTable(), value,value);
        logger.info(sql);
        try (Connection c = ds.getConnection();
            PreparedStatement ps = c.prepareStatement(sql)) {
//            ps.setString(1, value);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                MySQLUserRepresentation u = new MySQLUserRepresentation();
                u.setId(rs.getString("id"));
                u.setUsername(rs.getString("username"));
                u.setPasswordHash(rs.getString("password"));
                u.setEmail(rs.getString("email"));
                u.setFirstName(rs.getString("firstName"));
                u.setLastname(rs.getString("lastName"));
                return u;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    private MySQLUserRepresentation findUserByCode(String companyCode,String memberCode) {
        String sql = String.format("SELECT id, %s AS username, %s AS password , %s AS email , %s AS firstName, %s AS lastName FROM %s WHERE company_code = '%s' and kaiin_code = '%s'",
                config.getUsernameField(), config.getPasswordField(),config.getEmailField(),config.getFirstNameField(),config.getLastNameField(),
                config.getUserTable(), companyCode,memberCode);
        logger.info(sql);
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
//            ps.setString(1, value);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                MySQLUserRepresentation u = new MySQLUserRepresentation();
                u.setId(rs.getString("id"));
                u.setUsername(rs.getString("username"));
                u.setPasswordHash(rs.getString("password"));
                u.setEmail(rs.getString("email"));
                u.setFirstName(rs.getString("firstName"));
                u.setLastname(rs.getString("lastName"));
                return u;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    private UserModel createKeycloakUser(RealmModel realm, MySQLUserRepresentation u) {
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
                return u.getFirstname();
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
                u.setLastname(s);
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
                logger.info("super.isFederated():" + String.valueOf(super.isFederated()));
                return super.isFederated();
            }

            @Override
            public String getId() {
                return StorageId.keycloakId(model, u.getId());
            }
        };
        logger.info("info:" + u.getEmail() + " ," +u.getFirstname()+u.getLastName());

        adapter.setEnabled(true);
        adapter.setEmail(u.getEmail());
        adapter.setFirstName(u.getFirstname());
        adapter.setLastName(u.getLastName());
        return adapter;
    }

    @Override
    public void preRemove(RealmModel realm) {
        UserStorageProvider.super.preRemove(realm);
    }

    @Override
    public void preRemove(RealmModel realm, GroupModel group) {
        UserStorageProvider.super.preRemove(realm, group);
    }

    @Override
    public void preRemove(RealmModel realm, RoleModel role) {
        UserStorageProvider.super.preRemove(realm, role);
    }

    @Override
    public boolean supportsCredentialType(String credentialType) {
        return PasswordCredentialModel.TYPE.equals(credentialType);
    }

    @Override
    public boolean isConfiguredFor(RealmModel realm, UserModel user, String credentialType) {
        return supportsCredentialType(credentialType);
    }

    private MySQLUserRepresentation findUserByCompanyAndMember(String companyCode, String memberCode) {
        String sql = String.format("SELECT id, %s AS username, %s AS password , %s AS email , %s AS firstName, %s AS lastName FROM %s WHERE company_code = '%s' and kaiin_code = '%s'",
                config.getUsernameField(), config.getPasswordField(),config.getEmailField(),config.getFirstNameField(),config.getLastNameField(),
                config.getUserTable(), companyCode,memberCode);
        logger.info(sql);
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                MySQLUserRepresentation u = new MySQLUserRepresentation();
                u.setId(rs.getString("id"));
                u.setUsername(rs.getString("username"));
                u.setPasswordHash(rs.getString("password"));
                u.setEmail(rs.getString("email"));
                u.setFirstName(rs.getString("firstName"));
                u.setLastname(rs.getString("lastName"));
                return u;
            }catch (Exception ex){
                logger.info(ex.getMessage());
                throw new RuntimeException(ex);
            }
        } catch (SQLException e) {
            logger.info(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isValid(RealmModel realm, UserModel user, CredentialInput input) {
        logger.info("isValid**********************************************");
        if (!supportsCredentialType(input.getType()) || !(input instanceof UserCredentialModel)) {
            return false;
        }

        String username = user.getUsername();
        String inputPassword = ((UserCredentialModel) input).getChallengeResponse();
        if(username.contains(",")){
            String[] codeArry=username.split(",");
            MySQLUserRepresentation u = findUserByCode(codeArry[0],codeArry[1]);

            logger.info("username:" + username);
            logger.info("u.password:" + u.getPasswordHash());
            logger.info("inputPassword:" + inputPassword);
            return inputPassword.equals(u.getPasswordHash());
        }else{
            MySQLUserRepresentation u = findUser(username);
            logger.info("username:" + username);
            logger.info("u.password:" + u.getPasswordHash());
            logger.info("inputPassword:" + inputPassword);
            return inputPassword.equals(u.getPasswordHash());
        }


//        return true;
//        // 从数据库重新读取密码哈希
//        MySQLUserRepresentation dbUser = findUser("id", StorageId.externalId(user.getId()));
//        if (dbUser == null) return false;
//
//        // 使用 bcrypt 验证密码
//        return BCrypt.verifyer()
//                .verify(inputPassword.toCharArray(), dbUser.getPasswordHash())
//                .verified;

//        logger.info("loginType");
//        logger.info(((UserCredentialModel) input).getNote("loginType"));
//        logger.info("username");
//        logger.info(((UserCredentialModel) input).getNote("username"));
//        logger.info("password");
//        logger.info(((UserCredentialModel) input).getNote("password"));
//        logger.info("companyCode");
//        logger.info(((UserCredentialModel) input).getNote("companyCode"));

        //如果采用自定义的前端界面和验证流程，流程中如果没有keycloak这个标准的user password form这个exe的话
        //也就不会进入这个函数，传递前端参数值过来，也就没办法验证密码
        //但是如果流程中采用了keycloak这个标准的user password form，也就会多出一个确认界面
        //所以如果需要全定制化干脆直接就在CompanyCodeAuthenticator中连接外部数据库进行验证
        //但是这样就完全脱离了keyclaok的现有标准的验证流程，是一个全定制化的流程了
        //因此这个SPI作为一个采用标准流程的SPI暂时留下，前端界面就算采用了定制化界面，也是传递不过来companycode和membercode的
        //除非前端把companycode和membercode组合成username并且明确传递过来

//        String loginType = ((UserCredentialModel) input).getNote("loginType").toString();
//        String password = ((UserCredentialModel) input).getNote("password").toString();
//
//        if ("company".equals(loginType)) {
//            String companyCode = ((UserCredentialModel) input).getNote("companyCode").toString();
//            String memberCode = ((UserCredentialModel) input).getNote("memberCode").toString();
//
//            // 用 companyCode + memberCode 查询数据库
//            MySQLUserRepresentation u = findUserByCompanyAndMember(companyCode, memberCode);
//            return password.equals(u);
//        } else {
//            String username = ((UserCredentialModel) input).getNote("username").toString();
//            MySQLUserRepresentation u = findUser(username);
//            return password.equals(u);
//        }
    }
}
