package com.partion.ledger.dto;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class LedgerBlockResponse {

    private final Long id;
    private final Long blockNumber;
    private final String previousHash;
    private final String merkleRoot;
    private final String currentHash;
    private final LocalDateTime createdAt;
    private final Long eventCount;
    private final List<LedgerTransactionResponse> transactions;

    public LedgerBlockResponse(
            LedgerBlockRow row,
            List<LedgerTransactionResponse> transactions
    ) {
        this.id = row.getId();
        this.blockNumber = row.getBlockNumber();
        this.previousHash = row.getPreviousHash();
        this.merkleRoot = row.getMerkleRoot();
        this.currentHash = row.getCurrentHash();
        this.createdAt = row.getCreatedAt();
        this.eventCount = row.getEventCount();
        this.transactions = transactions;
    }
}