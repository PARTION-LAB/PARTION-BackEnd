package com.partion.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OAuthAuthorizationUrlResponse {
    private String authorizationUrl;
    private String state;
}