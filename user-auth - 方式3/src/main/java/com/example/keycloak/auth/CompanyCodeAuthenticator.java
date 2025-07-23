package com.example.keycloak.auth;

import com.example.UserRepresentation;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.core.MultivaluedMap;
import org.jboss.logging.Logger;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.Authenticator;
import org.keycloak.models.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.core.type.TypeReference;
public class CompanyCodeAuthenticator implements Authenticator {
    private static final Logger logger = Logger.getLogger(CompanyCodeAuthenticator.class);
    private final KeycloakSession session;
    public CompanyCodeAuthenticator(KeycloakSession session) {
        this.session = session;
    }

    @Override
    public void authenticate(AuthenticationFlowContext context) {
//        context.form().createForm("login.ftl");
        logger.info("come to authenticate function，show the login form" );
        context.challenge(context.form().createForm("login.ftl"));
    }

    @Override
    public void action(AuthenticationFlowContext context) {

        logger.info("come to action function" );

        // get the tab index
        String tab = context.getHttpRequest().getDecodedFormParameters().getFirst("loginType");

        // user info params
        String username = null;
        String password = null;
        String companyCode=null;
        String memberCode=null;

        //user object
        UserModel user=null;
        RealmModel realm = context.getRealm();

        //sql for searching user by foreigner DB
        String sql ="";

        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();

        logger.info("tab:" + tab);
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
        logger.info("sql:" + sql);
        // if you want to check the local DB user
//        if(username!=null) {
//            UserModel userLocal = session.users().getUserByUsername(realm, username);
//
//            if (userLocal != null) {
//                logger.info("进入action,userLocal=" + userLocal.getUsername());
//            } else {
//                logger.info("userLocal == null");
//            }
//            logger.info("登录框中的用户名：" + context.getHttpRequest().getDecodedFormParameters().getFirst("username"));
//
//            //if the local user exist
//            if (userLocal != null) {
//                logger.info("存在本地用户:" + userLocal.getUsername() + "," + userLocal.getFirstName());
//                context.setUser(userLocal);
//                context.success();
//                return;
//            }
//        }

        //if the local user not exist,connect the foreigner DB and search
        logger.info("search the foreigner DB");
        // the select form index
        String selectedUserId = context.getHttpRequest().getDecodedFormParameters().getFirst("selectedUserId");

        //connection to foreigner DB
        Connection connection = null;
        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/mochikabu?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC", "root", "Deloitte2017");
        } catch (SQLException e) {
            logger.info("DB connect erro");
            throw new RuntimeException("Failed to connect to MySQL database", e);
        }

        logger.info("selectedUserId:" + selectedUserId);
        // if multiusers are exist , after selected the company
        //step 2: if multiple users exist, show select form and login****************************
        if (selectedUserId != null) {
            logger.info("after selected the company" );
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

            password=selectedUser.getPasswordHash();
            logger.info("the user seleceted:" + selectedUser.getUsername());
            user = session.users().getUserByUsername(realm, selectedUser.getUsername());

            logger.info("now the user is ；" + user.getUsername());
            if (user == null) {
                context.failureChallenge(AuthenticationFlowError.INVALID_USER,
                        context.form().setError("ユーザが存在してない1").createForm("login.ftl"));
                return;
            }

            logger.info("user federation name:" + user.getUsername());
            context.setUser(user);

            //login succeed
            context.success();

        } else {
            logger.info("step1");

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
                            context.form().setError("ユーザが存在してない").createForm("login.ftl"));
                    return;
                }else if(count==1){
                    //if there is 1 matched user
                    logger.info("find a user：" + users.get(0).getUsername());
                    user = session.users().getUserByUsername(realm, users.get(0).getUsername());

                    // set the login user
                    context.setUser(user);

                    //login succeeded
                    context.success();
                    return;
                }else {
                    // if there are multiple users, show the company select form
                    logger.info("begin to show the company select form");
                    // save the users info to  session
                    String jsonUsers = new ObjectMapper().writeValueAsString(users);
                    context.getAuthenticationSession().setAuthNote("USER_CANDIDATES", jsonUsers);

                    // show form
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

        logger.info("federation user:" + user.getUsername());
        String[] codeArry=user.getUsername().split("_");
        //sear the foreigner DB and check the password match or not
        sql ="SELECT password FROM users WHERE company_code='"+ codeArry[0] + "' and kaiin_code='"+ codeArry[1] + "';";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        //don't find the user
                        context.failureChallenge(AuthenticationFlowError.INVALID_USER,
                                context.form().setError("ユーザが存在してない2").createForm("login.ftl"));
                        return;
                    }else if(!password.equals(rs.getString("password")))
                    {
                        //don't match
                        context.failureChallenge(AuthenticationFlowError.INVALID_USER,
                                context.form().setError("ユーザもしくはパスワードが正しくありません").createForm("login.ftl"));
                        return;
                    }
                }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to connect to MySQL database", e);
        }
        logger.info(sql);

        // set the login user at last
        context.setUser(user);

        //set login succeeded
        context.success();

    }

    @Override public void close() {}
    @Override public boolean requiresUser() { return false; }
    @Override public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) { return true; }
    @Override public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {}

}
