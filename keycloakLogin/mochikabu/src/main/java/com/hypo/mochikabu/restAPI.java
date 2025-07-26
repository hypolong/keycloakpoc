package com.hypo.mochikabu;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.hypo.mochikabu.entity.User;
import com.hypo.mochikabu.entity.Repository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;

import javax.ws.rs.core.Response;

import java.net.URI;
import java.util.*;

@RestController
//@CrossOrigin
@CrossOrigin(origins = "*")
//@CrossOrigin(origins = "http://localhost:3000")
public class restAPI {

    @Autowired
    private keyGen getkey;

    @Autowired
    private Repository repository;

    @Autowired
    private TokenExchangeService tokenExchangeService;

    @Autowired
    private TokenExchangeServiceUser tokenExchangeServiceUser;

    @Value("${keycloak.auth-server-url}")
    private String keycloakAuthServerUrl;

    @Value("${keycloak.realm}")
    private String keycloakRealm;

    @GetMapping("/hello")
    public String hello() {
        return "Hello, World!";
    }

    @PostMapping("/greet")
    public String greet(@RequestParam(name = "name", defaultValue = "World") String name) {
        return "Hello, " + name + "!";
    }

    //创建用户的接口
    @PostMapping("/createuser")
    public ResponseEntity<?> createUser(@RequestBody User user) {
//        if (user.getId() != null) {
//            Map<String, String> error = new HashMap<>();
//            error.put("error", "新增用户时不能传入 ID，请去掉 id 字段");
//            return ResponseEntity.badRequest().body(error);
//        }
//        User saved = repository.save(user); // 只有 ID 为 null，才会执行插入
//        return ResponseEntity.ok(saved);
        System.out.println("进来了");
        System.out.println(user);
        User saved = repository.save(user);
        return ResponseEntity.ok(saved);
    }

    //返回带参数的URL的接口
    @PostMapping("/getURL")
    public String getURL(@RequestBody createURL urlInfo) {
        System.out.print(urlInfo.getURL());
        return urlInfo.getURL() + "?from=mochikabu&uid=hypo";
    }

    //返回带参数的URL的接口
    @PostMapping("/getKey")
    public String getKey(@RequestBody User user) {
        try {
            System.out.print(user.getUsername());
            System.out.print(user.getPassword());
//            System.out.print(repository.findByName(user.getName()).get(0));
            //查询数据库验证
            if (repository.findByUsername(user.getUsername()).size() > 0) {
                User get_user=repository.findByUsername(user.getUsername()).get(0);
                System.out.print(get_user.getUsername());
                System.out.print(get_user.getPassword());
                if(get_user.getPassword().equalsIgnoreCase(user.getPassword())) {
                    //根据获取到的信息，生成token
                    return getkey.generateToken(user.getUsername());
                }else{
                    return "the username or  pwd is not right";
                }
            } else {
                //根据获取到的信息，生成token
                return "user is not exist!";
            }
        }catch (Exception ex){
            return "user is not exist!";
        }
    }

    //验证token并查询数据库
    @GetMapping("/getUserInfo")
    public String getUserInfo(@RequestParam(name = "token", defaultValue = "hypolong") String token) {
        if(getkey.validateToken(token)) {
            User getUser=repository.findByUsername(getkey.getUsernameFromToken(token)).get(0);
            return "token validation ok , return userInfo, [Name:" + getUser.getUsername() + "] [Email:" + getUser.getEmail() + "] [phone:" + getUser.getPhone() +"]";
        }else{
            return "token validation failed!";
        }
    }

    //验证用户名和密码
    @PostMapping("/userVerify")
    public short userVerify(@RequestBody User user) {
        try {
            System.out.print(user.getUsername());
            if(user.getUsername().equalsIgnoreCase("") || user.getPassword().equalsIgnoreCase("")) {
                return 3; //"username or pwd cannot be null!";
            }
            else if (repository.findByUsername(user.getUsername()).size() > 0) {
                User get_user=repository.findByUsername(user.getUsername()).get(0);
                System.out.print(get_user.getUsername());
                System.out.print(get_user.getPassword());
                if(get_user.getPassword().equalsIgnoreCase(user.getPassword())) {
                    return 1;//succed
                }else{
                    return 0;//failed
                }
            } else {
                //user not found
                return 4;
            }
        }catch (Exception ex){
            return 5; //erro occured
        }
    }

    //验证是否可以实现批量导入用户到IdP--keycloak****************可以批量生成用户到keycloak
    @PostMapping("/creatUsersIdP")
    public String creatUsersIdP(@RequestBody User user) {
        try {
            Keycloak keycloak = Keycloak.getInstance(keycloakAuthServerUrl,
                    keycloakRealm,
                    "hypolong",
                    "Deloitte2017",
                    "mybackend_from",
                    "0eg2Nj9cioWrnJ4DBdyB1E9bs03J9poV");
            UserRepresentation users = new UserRepresentation();
            users.setUsername(user.getUsername());
            users.setFirstName("Hash");
            users.setLastName("Doe");
            users.setId(user.getUsername());
//            user.setPwd(user.getPwd()); //直接设置无用
            //设置客制化属性的值
            Map<String, List<String>> customAttributes = new HashMap<>();
            customAttributes.put("enterpriseCode", List.of("mochikabu1"));
            customAttributes.put("phone", List.of("090-1234-5678"));
            users.setAttributes(customAttributes);
            System.out.println("设置属性为：" + users.getAttributes());
            users.setEnabled(true);

//            String passwordCredentials = java.util.Base64.getEncoder().encodeToString((user.getName() + ":" + user.getPwd()).getBytes()); // 创建密码凭证（不推荐直接存储密码）
//            users.setCredentials(Collections.singletonList(passwordCredentials)); // 注意：这种方式不推荐，实际应用中应该使用更安全的密码管理方式。
            Response response = keycloak.realm(keycloakRealm).users().create(users);
            System.out.println(response.getHeaders());
            if (response.getStatus() == 201) { // 201 表示创建成功
                System.out.println("User created successfully" );
                //打印用户详细信息
                URI location = response.getLocation();
                String path = location.getPath();
                String userId = path.substring(path.lastIndexOf("/") + 1);
                System.out.println("✅ 用户ID: " + userId);

                UserResource userResource = keycloak.realm(keycloakRealm).users().get(userId);
                // 重新设置新密码
                CredentialRepresentation newPassword = new CredentialRepresentation();
                newPassword.setTemporary(false);
                newPassword.setType(CredentialRepresentation.PASSWORD);
                newPassword.setValue(user.getPassword());
                userResource.resetPassword(newPassword);
                System.out.println("✅ 密码已更新");

                System.out.println("✔️ 用户客制化属性：" + userResource.toRepresentation().getAttributes());

                return "succeed";
            } else {
                System.out.println("Failed to create user, return erro code:"+response.getStatus());
                return "failed";
            }
        }catch (Exception ex){
            System.out.print(" Erro:"+ex.getMessage());
            return "failed add user";
        }
    }
    //创建并定制用户属性
    @PostMapping("/addUsersAttr")
    public String addUsersAttr(@RequestBody User user) {
        try {
            Keycloak keycloak = Keycloak.getInstance(keycloakAuthServerUrl,
                    keycloakRealm,
                    "hypolong",
                    "Deloitte2017",
                    "mybackend_from",
                    "0eg2Nj9cioWrnJ4DBdyB1E9bs03J9poV");
            UserRepresentation users = new UserRepresentation();
            users.setUsername(user.getUsername());
            users.setFirstName("John");
            users.setLastName("Doe");
            users.setEnabled(true);

//            String passwordCredentials = java.util.Base64.getEncoder().encodeToString((user.getName() + ":" + user.getPwd()).getBytes()); // 创建密码凭证（不推荐直接存储密码）
//            users.setCredentials(Collections.singletonList(passwordCredentials)); // 注意：这种方式不推荐，实际应用中应该使用更安全的密码管理方式。
            Response response = keycloak.realm(keycloakRealm).users().create(users);
            if (response.getStatus() == 201) { // 201 表示创建成功
                System.out.println("User created successfully");
                return "succeed";
            } else {
                System.out.println("Failed to create user, return erro code:"+response.getStatus());
                return "failed";
            }
        }catch (Exception ex){
            System.out.print(" Erro:"+ex.getMessage());
            return "failed add user";
        }
    }

    //token exchange
    @PostMapping("/accessTokenGet")
    public String accessTokenGet(@RequestBody User user) {
        try {
            String access_token="";
            if(user.getUsername()!="") {
                Keycloak keycloak = Keycloak.getInstance(keycloakAuthServerUrl,
                        keycloakRealm,
                        user.getUsername(),
                        user.getPassword(),
                        "mybackend_from",
                        "0eg2Nj9cioWrnJ4DBdyB1E9bs03J9poV");
            access_token= keycloak.tokenManager().getAccessToken().getToken();

            }else{
                access_token = new KeycloakTokenFetcher().getAccessToken().block(); // 阻塞调用
            }
            System.out.println(access_token);
            String[] parts = access_token.split("\\.");
            String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]));
            ObjectMapper mapper = new ObjectMapper();
            access_token = mapper.readTree(payloadJson).toString();
            if (access_token!="") {
                System.out.println("token get OK" + "\n");
                return "succeed" + access_token;
            } else {
                System.out.println("token get NG");
                return "failed";
            }
        }catch (Exception ex){
            System.out.print(" Erro:"+ex.getMessage());
            return "failed add user";
        }
    }

    //token exchange: client to client
    @PostMapping("/exchangeToken")
    public String exchangeToken(@RequestBody User user) {
        try {
            String access_token="";
            if(user.getUsername()!="") {
                Keycloak keycloak = Keycloak.getInstance(keycloakAuthServerUrl,
                        keycloakRealm,
                        user.getUsername(),
                        user.getPassword(),
                        "mybackend_from",
                        "0eg2Nj9cioWrnJ4DBdyB1E9bs03J9poV");
                access_token= keycloak.tokenManager().getAccessToken().getToken();
                //转换为json
                String[] parts = access_token.split("\\.");
                String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]));
                ObjectMapper mapper = new ObjectMapper();
                var payload = mapper.readTree(payloadJson);
                System.out.println("before exchange:" + "\n");
                System.out.println(payload.toPrettyString());
                if (access_token!=""){
                    //token进行交换
                    String clientBToken = tokenExchangeService.exchangeToken(access_token);
                    if (clientBToken!="") { // 201 表示创建成功
                        System.out.println("token exchange OK" + "\n");
                        System.out.println(clientBToken + "\n");
                        //转换为json
                        parts = clientBToken.split("\\.");
                        payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]));
                        mapper = new ObjectMapper();
                        payload = mapper.readTree(payloadJson);
                        System.out.println("after exchange:" + "\n");
                        System.out.println(payload.toPrettyString());
                        return "succeed"+payloadJson.toString();
                    } else {
                        System.out.println("token exchange NG");
                        return "failed";
                    }
                }else {
                    System.out.println("token exchange NG");
                    return "failed";
                }
            }else{
                access_token = new KeycloakTokenFetcher().getAccessToken().block(); // 阻塞调用
                //转换为json
                String[] parts = access_token.split("\\.");
                String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]));
                ObjectMapper mapper = new ObjectMapper();
                var payload = mapper.readTree(payloadJson);
                System.out.print("change_before:" +  "\n");
                System.out.println(payload.toPrettyString());
                if (access_token!=""){
                    //token进行交换
                    String clientBToken = tokenExchangeServiceUser.exchangeToken(access_token);
                    if (clientBToken!="") {
                        System.out.println("token exchange OK" + "\n");
//                        System.out.println(clientBToken + "\n");
                        //转换为json
                        parts = clientBToken.split("\\.");
                        payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]));
                        mapper = new ObjectMapper();
                        payload = mapper.readTree(payloadJson);
                        System.out.print("change_after:" +  "\n");
                        System.out.println(payload.toPrettyString());
                        return "succeed"+payloadJson.toString();
                    } else {
                        System.out.println("token exchange NG");
                        return "failed";
                    }
                }else {
                    System.out.println("token exchange NG");
                    return "failed";
                }
            }



        }catch (Exception ex){
            System.out.print(" Erro:"+ex.getMessage());
            return "failed add user";
        }
    }

    //token exchange: client to client
    @PostMapping("/exchangeTokenUser")
    public String exchangeTokenUser(@RequestBody User user) {
        try {
            Keycloak keycloak = Keycloak.getInstance(keycloakAuthServerUrl,
                    keycloakRealm,
                    "test_user1",
                    "test01",
                    "mybackend_from",
                    "0eg2Nj9cioWrnJ4DBdyB1E9bs03J9poV");
            //先获取这个client的access token
            String access_token=keycloak.tokenManager().getAccessToken().getToken();

            //token进行交换,指定用户
            String clientBToken = tokenExchangeServiceUser.exchangeToken(access_token);
            if (clientBToken!="") { // 201 表示创建成功
                System.out.println("token exchange OK" + "\n");
                System.out.println(clientBToken + "\n");
                //转换为json
                String[] parts = clientBToken.split("\\.");

                String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]));
                ObjectMapper mapper = new ObjectMapper();
                var payload = mapper.readTree(payloadJson);
                System.out.println(payload.toPrettyString());
                return "succeed";
            } else {
                System.out.println("token exchange NG");
                return "failed";
            }
        }catch (Exception ex){
            System.out.print(" Erro:"+ex.getMessage());
            return "failed add user";
        }
    }
}
