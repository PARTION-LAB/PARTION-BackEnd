package com.partion.ledger.controller;

import com.partion.global.response.PageResponse;
import com.partion.ledger.dto.LedgerBlockResponse;
import com.partion.ledger.dto.LedgerTransactionResponse;
import com.partion.ledger.dto.LedgerVerifyResponse;
import com.partion.ledger.service.LedgerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ledger")
public class LedgerController {

    private final LedgerService ledgerService;

    @GetMapping("/blocks")
    public ResponseEntity<PageResponse<LedgerBlockResponse>> getBlocks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(ledgerService.getBlocks(page, size));
    }

    @GetMapping("/blocks/{blockNumber}")
    public ResponseEntity<LedgerBlockResponse> getBlock(
            @PathVariable Long blockNumber
    ) {
        return ResponseEntity.ok(ledgerService.getBlock(blockNumber));
    }

    @GetMapping("/transactions")
    public ResponseEntity<PageResponse<LedgerTransactionResponse>> getTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(ledgerService.getTransactions(page, size));
    }

    @GetMapping("/transactions/{transactionHash}")
    public ResponseEntity<LedgerTransactionResponse> getTransaction(
            @PathVariable String transactionHash
    ) {
        return ResponseEntity.ok(ledgerService.getTransaction(transactionHash));
    }

    @GetMapping("/verify")
    public ResponseEntity<LedgerVerifyResponse> verify() {
        return ResponseEntity.ok(ledgerService.verify());
    }
}