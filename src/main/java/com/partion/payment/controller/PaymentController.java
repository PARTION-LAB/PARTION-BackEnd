package com.partion.payment.controller;

import com.partion.global.response.PageResponse;
import com.partion.global.security.CustomUserDetails;
import com.partion.payment.dto.*;
import com.partion.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping("/deposits/me")
    public ResponseEntity<PageResponse<DepositHistoryResponse>> getMyDepositHistories(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        PageResponse<DepositHistoryResponse> response =
                paymentService.getMyDepositHistories(
                        userDetails.getMemberId(),
                        page,
                        size
                );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/deposits/ready")
    public ResponseEntity<DepositReadyResponse> readyDeposit(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody DepositReadyRequest request
    ) {
        DepositReadyResponse response =
                paymentService.readyDeposit(userDetails.getMemberId(), request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/deposits/confirm")
    public ResponseEntity<DepositConfirmResponse> confirmDeposit(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody DepositConfirmRequest request
    ) {
        DepositConfirmResponse response =
                paymentService.confirmDeposit(userDetails.getMemberId(), request);

        return ResponseEntity.ok(response);
    }
}