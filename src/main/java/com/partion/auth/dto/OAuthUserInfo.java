package com.partion.auth.dto;

import lombok.Getter;

@Getter
public class OAuthUserInfo {

    private final String provider;
    private final String providerId;
    private final String email;

    public OAuthUserInfo(String provider, String providerId, String email) {
        this.provider = provider;
        this.providerId = providerId;
        this.email = email;
    }
}