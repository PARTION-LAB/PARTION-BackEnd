package com.partion.ledger.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class LedgerTransactionRow {

    private Long id;
    private Long blockId;
    private String transactionHash;
    private String payloadHash;
    private String eventType;
    private String referenceType;
    private Long referenceId;
    private String payloadJson;
    private LocalDateTime createdAt;
}