package com.example.keycloak.auth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.core.MultivaluedMap;
import org.jboss.logging.Logger;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.Authenticator;
import org.keycloak.models.*;

//other interfaces you may want to use
//import org.keycloak.credential.CredentialModel;
//import org.keycloak.credential.PasswordCredentialProvider;
//import org.keycloak.credential.hash.PasswordHashProvider;
//import org.keycloak.crypto.HashProvider;
//import org.keycloak.models.credential.PasswordCredentialModel;
//import org.keycloak.models.utils.KeycloakModelUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CompanyCodeAuthenticator implements Authenticator {
    private static final Logger logger = Logger.getLogger(CompanyCodeAuthenticator.class);
    private final KeycloakSession session;
    public CompanyCodeAuthenticator(KeycloakSession session) {
        this.session = session;
    }

    @Override
    public void authenticate(AuthenticationFlowContext context) {
        logger.info("1.come to authenticate function，show the login form" );
        context.challenge(context.form().createForm("login.ftl"));
    }

    @Override
    public void action(AuthenticationFlowContext context) {

        logger.info("come to action function");

        //user to be return to session
        UserModel user=null;

        // current realm
        RealmModel realm = context.getRealm();

        // step 1:find user************************************************************************************
        String tab = context.getHttpRequest().getDecodedFormParameters().getFirst("loginType");

        //credential info get from the login form
        String username = "";
        String password = "";
        String companyCode="";
        String memberCode="";

        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();

        //sql for searching user by foreigner DB
        String sql ="";

        if ("company".equals(tab)) {
            companyCode = formData.getFirst("companyCode");
            memberCode = formData.getFirst("memberCode");
            username = companyCode + "_" + memberCode;
            password = formData.getFirst("password2");  //be careful this param is password2--the tab 2 password
            sql ="SELECT * FROM users WHERE company_code='"+ companyCode +"' and kaiin_code='" + memberCode +"' and password='"+password+"';";
        } else {
            username = formData.getFirst("username");
            password = formData.getFirst("password");
            sql ="SELECT * FROM users WHERE email = '"+ username +"' and password='"+password+"';";
        }

        // ①check out whether the user is keycloak local user  查找是否是本地用户（Keycloak DB 中存在的用户）,仅仅当选择的是邮箱密码
        if(username!=null) {
            UserModel userLocal = session.users().getUserByUsername(realm, username);

            if (userLocal != null) {
                logger.info("进入action,userLocal=" + userLocal.getUsername());
            } else {
                logger.info("userLocal == null");
            }
            logger.info("the username of login form：" + context.getHttpRequest().getDecodedFormParameters().getFirst("username"));

            //if the local user exist
            if (userLocal != null) {
                logger.info("exist in local DB:" + userLocal.getUsername() + "," + userLocal.getFirstName());
                context.setUser(userLocal);
                context.success();
                return;
            }
        }

        //if the local user not exist,connect the foreigner DB and search
        logger.info("Don't exist in local DB,connect the foreigner DB to search user");

        // the select form index
        String selectedUserId = context.getHttpRequest().getDecodedFormParameters().getFirst("selectedUserId");

        //connection to foreigner DB
        Connection connection = null;
        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/mochikabu?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC", "root", "Deloitte2017");
        } catch (SQLException e) {
            logger.info("DB connect erro occured");
            throw new RuntimeException("Failed to connect to MySQL database", e);
        }

        logger.info("selectedUserId:" + selectedUserId);
        // if multiusers are exist , after selected the company
        //step 2: if multiple users exist, show select form and login****************************
        if (selectedUserId != null) {
            logger.info("After select the company" );
            String usersJson = context.getAuthenticationSession().getAuthNote("USER_CANDIDATES");
            logger.info(usersJson);
            ObjectMapper mapper = new ObjectMapper();
            List<UserRepresentation> users = new ArrayList<>();
            try {
                users = mapper.readValue(usersJson, new TypeReference<List<UserRepresentation>>() {});
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }

            UserRepresentation selectedUser = users.stream()
                    .filter(u -> u.getId().equals(selectedUserId))
                    .findFirst()
                    .orElse(null);

            if (selectedUser == null) {
                context.failureChallenge(AuthenticationFlowError.INVALID_USER,
                        context.form().setError("選択したユーザが見つかりません").createForm("login.ftl"));
                return;
            }


            logger.info("the user selected:" + selectedUser.getUsername());
            user = session.users().getUserByUsername(realm, selectedUser.getUsername());
            if(user==null) {
                //if the user is not added to keycloak,then add it
                user = session.users().addUser(realm, selectedUser.getUsername());
                //if you want to add this user ,the email must not  grant to multiple users
                //user.setEmail(selectedUser.getEmail());
                user.setFirstName(selectedUser.getFirstName());
                user.setLastName(selectedUser.getLastName());
                user.setEnabled(true);

                //you can set password if you want
                //password=selectedUser.getPasswordHash();
            }

            logger.info("if still the user is null, then erro");
            if (user == null) {
                context.failureChallenge(AuthenticationFlowError.INVALID_USER,
                        context.form().setError("ユーザが存在してない1").createForm("login.ftl"));
                return;
            }

            logger.info("user federation at last:" + user.getUsername());
            context.setUser(user);

            //set login succeeded
            context.success();

            try {
                connection.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        } else {
            logger.info("step 1");
            logger.info("serach SQL：" + sql);
            try {
                PreparedStatement ps = connection.prepareStatement(sql);
                ResultSet rs = ps.executeQuery();
                int count=0;
                List<UserRepresentation> users = new ArrayList<>();
                while (rs.next()) {
                    UserRepresentation u=new UserRepresentation();
                    u.setId(rs.getString("company_code") +"_" + rs.getString("kaiin_code"));
                    //attention: the company_code and kaain_code should not contain '_'
                    u.setUsername(rs.getString("company_code") +"_" + rs.getString("kaiin_code"));
                    u.setPasswordHash(rs.getString("password"));
                    u.setEmail(rs.getString("email"));
                    u.setFirstName(rs.getString("firstname"));
                    u.setLastName(rs.getString("lastname"));
                    u.setCompanyCode("企業コード：" + rs.getString("company_code"));
                    u.setMemberCode("会員コード：" +rs.getString("kaiin_code"));
                    users.add(u);

                    count++;
                }
                if (count==0) {
                    logger.info("the user is not exist in foreigner DB");
                    //if there is no matched user
                    context.failureChallenge(AuthenticationFlowError.INVALID_USER,
                            context.form().setError("ユーザが存在してない2").createForm("login.ftl"));
                    return;
                }else if(count==1){
                    //if there is 1 matched user
                    logger.info("find a user：" + users.get(0).getUsername());

                    user = session.users().getUserByUsername(realm, users.get(0).getUsername());
                    if(user==null) {
                        //if the user is not added to keycloak,then add it
                        user = session.users().addUser(realm, users.get(0).getUsername());
                        //if you want to add this user ,the email must not grant to multiple users or will  occur erro
                        //user.setEmail(users.get(0).getEmail());
                        user.setFirstName(users.get(0).getFirstName());
                        user.setLastName(users.get(0).getLastName());
                        //you can set the user attributes here
                         // user.setAttribute();
                        user.setEnabled(true);

                    }
                    logger.info("user federation at last；" + user.getUsername());

                    // set the current login user
                    context.setUser(user);

                    //set login succeeded
                    context.success();

                    //close the connection
                    //you should use the connection pool to defend the multi-thread erro in production evn
                   // connection.close();
                    return;
                }else {
                    // if there are multiple users, show the company select form
                    logger.info("show the company select form");
                    //save users info to session
                    String jsonUsers = new ObjectMapper().writeValueAsString(users);
                    context.getAuthenticationSession().setAuthNote("USER_CANDIDATES", jsonUsers);

                    //show the form
                    context.challenge(
                            context.form()
                                    .setAttribute("candidates", users)
                                    .createForm("select-user.ftl")
                    );
                    return;
                }
            }catch (Exception ex){
                logger.info("query erro："+ex.getMessage());
            }
            logger.info(sql);
        }

    }

    @Override public void close() {}
    @Override public boolean requiresUser() { return false; }
    @Override public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) { return true; }
    @Override public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {}

}
