package com.partion.auth.dto;

import lombok.Getter;

@Getter
public class TokenIssueResult {

    private final String accessToken;
    private final String refreshToken;
    private final String tokenType;
    private final long expiresIn;
    private final long refreshTokenMaxAge;

    public TokenIssueResult(
            String accessToken,
            String refreshToken,
            String tokenType,
            long expiresIn,
            long refreshTokenMaxAge
    ) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.tokenType = tokenType;
        this.expiresIn = expiresIn;
        this.refreshTokenMaxAge = refreshTokenMaxAge;
    }

    public TokenResponse toResponse() {
        return new TokenResponse(accessToken, tokenType, expiresIn);
    }
}