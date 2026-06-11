package com.partion.auth.controller;

import com.partion.auth.dto.*;
import com.partion.auth.service.AuthService;
import com.partion.global.exception.BusinessException;
import com.partion.global.exception.ErrorCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<SignupResponse> signup(@Valid @RequestBody SignupRequest request) {
        SignupResponse response = authService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        TokenIssueResult result = authService.login(request);

        ResponseCookie refreshCookie =
                createRefreshTokenCookie(result.getRefreshToken(), result.getRefreshTokenMaxAge());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(result.toResponse());
    }

    @PostMapping("/reissue")
    public ResponseEntity<AccessTokenResponse> reissue(
            @CookieValue(value = "refreshToken", required = false) String refreshToken
    ) {
        AccessTokenResponse response = authService.reissue(refreshToken);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

        String accessToken = authorizationHeader.substring(7);
        authService.logout(accessToken);

        ResponseCookie deleteCookie = deleteRefreshTokenCookie();

        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, deleteCookie.toString())
                .build();
    }

    @PostMapping("/password/reset")
    public ResponseEntity<PasswordResetResponse> resetPassword(
            @Valid @RequestBody PasswordResetRequest request
    ) {
        PasswordResetResponse response = authService.resetPassword(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/oauth/login")
    public ResponseEntity<TokenResponse> oauthLogin(
            @Valid @RequestBody OAuthLoginRequest request
    ) {
        TokenIssueResult result = authService.oauthLogin(request);

        ResponseCookie refreshCookie =
                createRefreshTokenCookie(result.getRefreshToken(), result.getRefreshTokenMaxAge());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(result.toResponse());
    }

    private ResponseCookie createRefreshTokenCookie(String refreshToken, long maxAgeSeconds) {
        return ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(Duration.ofSeconds(maxAgeSeconds))
                .sameSite("Lax")
                .build();
    }

    private ResponseCookie deleteRefreshTokenCookie() {
        return ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();
    }
}
