package com.partion.auth.dto;

import lombok.Getter;

@Getter
public class PasswordResetResponse {

    private final String email;

    public PasswordResetResponse(String email) {
        this.email = email;
    }
}