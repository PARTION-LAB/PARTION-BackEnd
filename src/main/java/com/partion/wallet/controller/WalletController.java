package com.partion.wallet.controller;

import com.partion.global.security.CustomUserDetails;
import com.partion.wallet.dto.WalletResponse;
import com.partion.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/wallets")
public class WalletController {

    private final WalletService walletService;

    @GetMapping("/me")
    public ResponseEntity<WalletResponse> getMyWallet(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        WalletResponse response = walletService.getMyWallet(userDetails.getMemberId());
        return ResponseEntity.ok(response);
    }
}
