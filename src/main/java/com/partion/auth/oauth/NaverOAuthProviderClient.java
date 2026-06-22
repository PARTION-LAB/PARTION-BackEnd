package com.partion.auth.oauth;

import com.partion.auth.domain.OAuthProvider;
import com.partion.auth.dto.OAuthUserInfo;
import com.partion.global.exception.BusinessException;
import com.partion.global.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

@Component
public class NaverOAuthProviderClient implements OAuthProviderClient {

    private final RestClient restClient = RestClient.create();

    @Value("${oauth.naver.client-id}")
    private String clientId;

    @Value("${oauth.naver.client-secret}")
    private String clientSecret;

    @Value("${oauth.naver.redirect-uri}")
    private String redirectUri;

    @Override
    public OAuthProvider provider() {
        return OAuthProvider.NAVER;
    }

    @Override
    public String createAuthorizationUrl(String state) {
        return UriComponentsBuilder
                .fromUriString("https://nid.naver.com/oauth2.0/authorize")
                .queryParam("response_type", "code")
                .queryParam("client_id", clientId)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("state", state)
                .build()
                .toUriString();
    }

    @Override
    public String requestAccessToken(String code, String state) {
        Map response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .host("nid.naver.com")
                        .path("/oauth2.0/token")
                        .queryParam("grant_type", "authorization_code")
                        .queryParam("client_id", clientId)
                        .queryParam("client_secret", clientSecret)
                        .queryParam("code", code)
                        .queryParam("state", state)
                        .build())
                .retrieve()
                .body(Map.class);

        if (response == null || response.get("access_token") == null) {
            throw new BusinessException(ErrorCode.OAUTH_LOGIN_FAILED);
        }

        return String.valueOf(response.get("access_token"));
    }

    @Override
    public OAuthUserInfo requestUserInfo(String accessToken) {
        Map response = restClient.get()
                .uri("https://openapi.naver.com/v1/nid/me")
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .body(Map.class);

        Map responseBody = (Map) response.get("response");

        String providerId = String.valueOf(responseBody.get("id"));
        String email = (String) responseBody.get("email");
        String nickname = (String) responseBody.get("nickname");

        if (email == null || email.isBlank()) {
            throw new BusinessException(ErrorCode.OAUTH_EMAIL_NOT_PROVIDED);
        }

        return new OAuthUserInfo("NAVER", providerId, email, nickname);
    }
}