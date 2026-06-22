package com.partion.auth.service;

import com.partion.auth.domain.OAuthProvider;
import com.partion.auth.dto.OAuthAuthorizationUrlResponse;
import com.partion.auth.dto.OAuthCodeLoginRequest;
import com.partion.auth.dto.OAuthUserInfo;
import com.partion.auth.dto.TokenIssueResult;
import com.partion.auth.oauth.OAuthProviderClient;
import com.partion.global.exception.BusinessException;
import com.partion.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OAuthService {

    private static final Duration STATE_TTL = Duration.ofMinutes(5);

    private final List<OAuthProviderClient> clients;
    private final StringRedisTemplate redisTemplate;
    private final AuthService authService;

    public OAuthAuthorizationUrlResponse createAuthorizationUrl(String providerValue) {
        OAuthProviderClient client = findClient(providerValue);

        String state = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(stateKey(client.provider(), state), "true", STATE_TTL);

        return new OAuthAuthorizationUrlResponse(
                client.createAuthorizationUrl(state),
                state
        );
    }

    public TokenIssueResult login(String providerValue, OAuthCodeLoginRequest request) {
        OAuthProviderClient client = findClient(providerValue);

        validateState(client.provider(), request.getState());

        try {
            String accessToken = client.requestAccessToken(request.getCode(), request.getState());
            OAuthUserInfo userInfo = client.requestUserInfo(accessToken);
            return authService.oauthLogin(userInfo);
        } finally {
            redisTemplate.delete(stateKey(client.provider(), request.getState()));
        }
    }

    private OAuthProviderClient findClient(String providerValue) {
        OAuthProvider provider = OAuthProvider.from(providerValue);

        return clients.stream()
                .filter(client -> client.provider() == provider)
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_OAUTH_PROVIDER));
    }

    private void validateState(OAuthProvider provider, String state) {
        Boolean exists = redisTemplate.hasKey(stateKey(provider, state));

        if (!Boolean.TRUE.equals(exists)) {
            throw new BusinessException(ErrorCode.OAUTH_LOGIN_FAILED);
        }
    }

    private String stateKey(OAuthProvider provider, String state) {
        return "oauth:" + provider.name() + ":state:" + state;
    }
}