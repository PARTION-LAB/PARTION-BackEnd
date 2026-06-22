package com.partion.auth.dto;

import com.partion.member.dto.MemberInfoResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TokenResponse {
    private String accessToken;
    private String tokenType;
    private long expiresIn;
    private MemberInfoResponse member;
}