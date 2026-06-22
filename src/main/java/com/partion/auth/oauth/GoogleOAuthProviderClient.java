package com.partion.auth.oauth;

import com.partion.auth.domain.OAuthProvider;
import com.partion.auth.dto.OAuthUserInfo;
import com.partion.global.exception.BusinessException;
import com.partion.global.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

@Component
public class GoogleOAuthProviderClient implements OAuthProviderClient {

    private final RestClient restClient = RestClient.create();

    @Value("${oauth.google.client-id}")
    private String clientId;

    @Value("${oauth.google.client-secret}")
    private String clientSecret;

    @Value("${oauth.google.redirect-uri}")
    private String redirectUri;

    @Override
    public OAuthProvider provider() {
        return OAuthProvider.GOOGLE;
    }

    @Override
    public String createAuthorizationUrl(String state) {
        return UriComponentsBuilder
                .fromUriString("https://accounts.google.com/o/oauth2/v2/auth")
                .queryParam("response_type", "code")
                .queryParam("client_id", clientId)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("scope", "openid email profile")
                .queryParam("state", state)
                .build()
                .encode()
                .toUriString();
    }

    @Override
    public String requestAccessToken(String code, String state) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "authorization_code");
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);
        form.add("redirect_uri", redirectUri);
        form.add("code", code);

        Map response = restClient.post()
                .uri("https://oauth2.googleapis.com/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
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
                .uri("https://www.googleapis.com/oauth2/v3/userinfo")
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .body(Map.class);

        if (response == null) {
            throw new BusinessException(ErrorCode.OAUTH_LOGIN_FAILED);
        }

        String providerId = String.valueOf(response.get("sub"));
        String email = (String) response.get("email");
        String nickname = (String) response.get("name");

        if (providerId == null || providerId.isBlank() || "null".equalsIgnoreCase(providerId)) {
            throw new BusinessException(ErrorCode.OAUTH_LOGIN_FAILED);
        }

        if (email == null || email.isBlank()) {
            throw new BusinessException(ErrorCode.OAUTH_EMAIL_NOT_PROVIDED);
        }

        return new OAuthUserInfo("GOOGLE", providerId, email, nickname);
    }
}