package com.partion.wallet.controller;

import com.partion.global.response.PageResponse;
import com.partion.global.security.CustomUserDetails;
import com.partion.wallet.dto.WalletResponse;
import com.partion.wallet.dto.WalletTransactionResponse;
import com.partion.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

    @GetMapping("/me/transactions")
    public ResponseEntity<PageResponse<WalletTransactionResponse>> getMyWalletTransactions(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        PageResponse<WalletTransactionResponse> response =
                walletService.getMyWalletTransactions(
                        userDetails.getMemberId(),
                        page,
                        size
                );

        return ResponseEntity.ok(response);
    }
}
