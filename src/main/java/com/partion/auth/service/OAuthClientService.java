package com.partion.auth.service;

import com.partion.auth.domain.OAuthProvider;
import com.partion.auth.dto.OAuthUserInfo;
import com.partion.global.exception.BusinessException;
import com.partion.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@RequiredArgsConstructor
@Service
public class OAuthClientService {

    private final RestClient restClient = RestClient.create();

    public OAuthUserInfo getUserInfo(String providerValue, String accessToken) {
        try {
            OAuthProvider provider = OAuthProvider.from(providerValue);

            if (provider == OAuthProvider.GOOGLE) {
                return getGoogleUserInfo(accessToken);
            }

            if (provider == OAuthProvider.NAVER) {
                return getNaverUserInfo(accessToken);
            }

            if (provider == OAuthProvider.KAKAO) {
                return getKakaoUserInfo(accessToken);
            }

            throw new BusinessException(ErrorCode.INVALID_OAUTH_PROVIDER);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.OAUTH_LOGIN_FAILED);
        }
    }

    private OAuthUserInfo getGoogleUserInfo(String accessToken) {
        Map response = restClient.get()
                .uri("https://www.googleapis.com/oauth2/v3/userinfo")
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .body(Map.class);

        String providerId = String.valueOf(response.get("sub"));
        String email = (String) response.get("email");

        validateEmail(email);

        return new OAuthUserInfo("GOOGLE", providerId, email);
    }

    private OAuthUserInfo getNaverUserInfo(String accessToken) {
        Map response = restClient.get()
                .uri("https://openapi.naver.com/v1/nid/me")
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .body(Map.class);

        Map responseBody = (Map) response.get("response");

        String providerId = String.valueOf(responseBody.get("id"));
        String email = (String) responseBody.get("email");

        validateEmail(email);

        return new OAuthUserInfo("NAVER", providerId, email);
    }

    private OAuthUserInfo getKakaoUserInfo(String accessToken) {
        Map response = restClient.get()
                .uri("https://kapi.kakao.com/v2/user/me")
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .body(Map.class);

        String providerId = String.valueOf(response.get("id"));
        Map kakaoAccount = (Map) response.get("kakao_account");
        String email = kakaoAccount == null ? null : (String) kakaoAccount.get("email");

        validateEmail(email);

        return new OAuthUserInfo("KAKAO", providerId, email);
    }

    private void validateEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new BusinessException(ErrorCode.OAUTH_EMAIL_NOT_PROVIDED);
        }
    }
}