package com.partion.wallet.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WalletTransaction {

    private Long id;
    private Long walletId;
    private String type;
    private BigDecimal amount;
    private BigDecimal availableBalanceAfter;
    private BigDecimal lockedBalanceAfter;
    private String referenceType;
    private Long referenceId;
    private LocalDateTime createdAt;
}