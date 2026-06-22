package com.partion.auth.oauth;

import com.partion.auth.domain.OAuthProvider;
import com.partion.auth.dto.OAuthUserInfo;

public interface OAuthProviderClient {
    OAuthProvider provider();

    String createAuthorizationUrl(String state);

    String requestAccessToken(String code, String state);

    OAuthUserInfo requestUserInfo(String accessToken);
}