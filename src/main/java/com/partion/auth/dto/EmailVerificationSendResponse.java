package com.partion.auth.dto;

import lombok.Getter;

@Getter
public class EmailVerificationSendResponse {

    private final String email;
    private final String purpose;
    private final long expiresIn;

    public EmailVerificationSendResponse(String email, String purpose, long expiresIn) {
        this.email = email;
        this.purpose = purpose;
        this.expiresIn = expiresIn;
    }
}