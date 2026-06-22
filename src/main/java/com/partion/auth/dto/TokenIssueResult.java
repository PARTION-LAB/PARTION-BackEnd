package com.partion.auth.dto;

import com.partion.member.dto.MemberInfoResponse;
import lombok.Getter;

@Getter
public class TokenIssueResult {

    private final String accessToken;
    private final String refreshToken;
    private final String tokenType;
    private final long expiresIn;
    private final long refreshTokenMaxAge;
    private final MemberInfoResponse member;

    public TokenIssueResult(
            String accessToken,
            String refreshToken,
            String tokenType,
            long expiresIn,
            long refreshTokenMaxAge,
            MemberInfoResponse member
    ) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.tokenType = tokenType;
        this.expiresIn = expiresIn;
        this.refreshTokenMaxAge = refreshTokenMaxAge;
        this.member = member;
    }

    public TokenResponse toResponse() {
        return new TokenResponse(accessToken, tokenType, expiresIn, member);
    }
}