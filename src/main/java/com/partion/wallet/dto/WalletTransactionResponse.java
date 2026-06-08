package com.partion.wallet.dto;

import com.partion.wallet.domain.WalletTransaction;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
public class WalletTransactionResponse {

    private final Long transactionId;
    private final String type;
    private final BigDecimal amount;
    private final BigDecimal availableBalanceAfter;
    private final BigDecimal lockedBalanceAfter;
    private final String referenceType;
    private final Long referenceId;
    private final LocalDateTime createdAt;

    public WalletTransactionResponse(WalletTransaction transaction) {
        this.transactionId = transaction.getId();
        this.type = transaction.getType();
        this.amount = transaction.getAmount();
        this.availableBalanceAfter = transaction.getAvailableBalanceAfter();
        this.lockedBalanceAfter = transaction.getLockedBalanceAfter();
        this.referenceType = transaction.getReferenceType();
        this.referenceId = transaction.getReferenceId();
        this.createdAt = transaction.getCreatedAt();
    }
}