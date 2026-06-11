package com.partion.auth.controller;

import com.partion.auth.dto.*;
import com.partion.auth.service.AuthService;
import com.partion.global.exception.BusinessException;
import com.partion.global.exception.ErrorCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
        TokenResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reissue")
    public ResponseEntity<AccessTokenResponse> reissue(@Valid @RequestBody ReissueRequest request) {
        AccessTokenResponse response = authService.reissue(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

        String accessToken = authorizationHeader.substring(7);
        authService.logout(accessToken);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/password/reset")
    public ResponseEntity<PasswordResetResponse> resetPassword(
            @Valid @RequestBody PasswordResetRequest request
    ) {
        PasswordResetResponse response = authService.resetPassword(request);
        return ResponseEntity.ok(response);
    }
}
