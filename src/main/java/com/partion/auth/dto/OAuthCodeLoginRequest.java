package com.partion.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class OAuthCodeLoginRequest {
    @NotBlank
    private String code;

    @NotBlank
    private String state;
}