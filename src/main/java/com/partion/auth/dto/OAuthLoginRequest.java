package com.partion.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class OAuthLoginRequest {

    @NotBlank(message = "OAuth 제공자는 필수입니다.")
    private String provider;

    @NotBlank(message = "Access Token은 필수입니다.")
    private String accessToken;

    @NotBlank(message = "닉네임은 필수입니다.")
    private String nickname;
}