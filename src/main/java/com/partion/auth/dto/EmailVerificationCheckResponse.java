package com.partion.auth.dto;

import lombok.Getter;

@Getter
public class EmailVerificationCheckResponse {

    private final String email;
    private final String purpose;
    private final boolean verified;
    private final long expiresIn;

    public EmailVerificationCheckResponse(String email, String purpose, boolean verified, long expiresIn) {
        this.email = email;
        this.purpose = purpose;
        this.verified = verified;
        this.expiresIn = expiresIn;
    }
}