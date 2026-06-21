package com.partion.ledger.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class LedgerBlockRow {

    private Long id;
    private Long blockNumber;
    private String previousHash;
    private String merkleRoot;
    private String currentHash;
    private LocalDateTime createdAt;
    private Long eventCount;
}