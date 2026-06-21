package com.partion.ledger.dto;

import lombok.Getter;

@Getter
public class LedgerVerifyResponse {

    private final boolean valid;
    private final long height;
    private final long checkedBlocks;
    private final String latestHash;
    private final String message;

    public LedgerVerifyResponse(
            boolean valid,
            long height,
            long checkedBlocks,
            String latestHash,
            String message
    ) {
        this.valid = valid;
        this.height = height;
        this.checkedBlocks = checkedBlocks;
        this.latestHash = latestHash;
        this.message = message;
    }
}