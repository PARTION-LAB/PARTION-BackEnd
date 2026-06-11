package com.partion.auth.controller;

import com.partion.auth.dto.EmailVerificationSendRequest;
import com.partion.auth.dto.EmailVerificationSendResponse;
import com.partion.auth.service.EmailVerificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth/email")
public class EmailVerificationController {

    private final EmailVerificationService emailVerificationService;

    @PostMapping("/send")
    public ResponseEntity<EmailVerificationSendResponse> sendVerificationCode(
            @Valid @RequestBody EmailVerificationSendRequest request
    ) {
        EmailVerificationSendResponse response =
                emailVerificationService.sendVerificationCode(request);

        return ResponseEntity.ok(response);
    }
}