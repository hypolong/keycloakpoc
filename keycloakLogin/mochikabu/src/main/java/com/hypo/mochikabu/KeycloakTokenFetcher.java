package com.hypo.mochikabu;


import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
public class KeycloakTokenFetcher {

    private final WebClient webClient = WebClient.create();
    @Value("${keycloak.auth-server-url}")
    private String keycloakAuthServerUrl;

    @Value("${keycloak.realm}")
    private String keycloakRealm;

    @Value("${keycloak.backend}")
    private String clientID;

    @Value("${keycloak.backend-secret}")
    private String clientSecret;


    public Mono<String> getAccessToken() {
        return webClient.post()
                .uri(keycloakAuthServerUrl+"/realms/"+keycloakRealm+"/protocol/openid-connect/token")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .bodyValue("grant_type=client_credentials&client_id=" + clientID + "&client_secret=" + clientSecret)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(json -> json.get("access_token").asText());
    }
}