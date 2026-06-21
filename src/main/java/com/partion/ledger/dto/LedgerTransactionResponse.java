package com.partion.ledger.dto;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class LedgerTransactionResponse {

    private final Long id;
    private final String transactionHash;
    private final String payloadHash;
    private final String eventType;
    private final String referenceType;
    private final Long referenceId;
    private final Object payload;
    private final LocalDateTime createdAt;

    public LedgerTransactionResponse(LedgerTransactionRow row, Object payload) {
        this.id = row.getId();
        this.transactionHash = row.getTransactionHash();
        this.payloadHash = row.getPayloadHash();
        this.eventType = row.getEventType();
        this.referenceType = row.getReferenceType();
        this.referenceId = row.getReferenceId();
        this.payload = payload;
        this.createdAt = row.getCreatedAt();
    }
}