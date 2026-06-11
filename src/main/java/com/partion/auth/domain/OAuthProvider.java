package com.partion.auth.domain;

import com.partion.global.exception.BusinessException;
import com.partion.global.exception.ErrorCode;

public enum OAuthProvider {
    GOOGLE,
    NAVER,
    KAKAO;

    public static OAuthProvider from(String provider) {
        try {
            return OAuthProvider.valueOf(provider.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new BusinessException(ErrorCode.INVALID_OAUTH_PROVIDER);
        }
    }
}