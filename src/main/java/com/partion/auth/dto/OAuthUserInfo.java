package com.partion.auth.dto;

import lombok.Getter;

@Getter
public class OAuthUserInfo {

    private final String provider;
    private final String providerId;
    private final String email;
    private final String nickname;

    public OAuthUserInfo(String provider, String providerId, String email) {
        this(provider, providerId, email, null);
    }

    public OAuthUserInfo(String provider, String providerId, String email, String nickname) {
        this.provider = provider;
        this.providerId = providerId;
        this.email = email;
        this.nickname = nickname;
    }
}