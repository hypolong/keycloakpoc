package com.hypo.mochikabu;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class TokenExchangeServiceUser {

    @Value("${keycloak.auth-server-url}")
    private String keycloakAuthServerUrl;

    @Value("${keycloak.realm}")
    private String keycloakRealm;

    @Value("${keycloak.backend}")
    private String clientAId;

    @Value("${keycloak.backend-secret}")
    private String clientASecret;

    @Value("${keycloak.client-b-id}")
    private String clientBId;

    public String exchangeToken(String clientAToken) {
        String url = String.format("%s/realms/%s/protocol/openid-connect/token", keycloakAuthServerUrl, keycloakRealm);

        // 构建请求体
        String body = String.format("client_id=%s&client_secret=%s&grant_type=urn:ietf:params:oauth:grant-type:token-exchange" +
                        "&subject_token=%s&subject_token_type=urn:ietf:params:oauth:token-type:access_token&requested_token_type=urn:ietf:params:oauth:token-type:access_token&audience=%s",
                clientAId, clientASecret, clientAToken, clientBId);

        // 创建请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // 创建请求体
        HttpEntity<String> entity = new HttpEntity<>(body, headers);

        // 使用RestTemplate发送请求
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

        // 返回获取到的Token
        if (response.getStatusCode() == HttpStatus.OK) {
            // 假设响应内容是JSON格式，其中包含了新的access_token
            String accessToken = extractAccessTokenFromResponse(response.getBody());
            return accessToken;
        } else {
            throw new RuntimeException("Token exchange failed: " + response.getStatusCode());
        }
    }

    private String extractAccessTokenFromResponse(String responseBody) {
        // 解析JSON响应，提取access_token字段
        // 这里只是简单的示例，实际情况可以使用JSON解析库如Jackson或Gson来处理
        int startIndex = responseBody.indexOf("\"access_token\":\"") + 16;
        int endIndex = responseBody.indexOf("\"", startIndex);
        return responseBody.substring(startIndex, endIndex);
    }
}
