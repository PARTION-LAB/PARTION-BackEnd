package com.partion.payment.dto;

import com.partion.payment.domain.DepositHistory;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
public class DepositHistoryResponse {

    private final Long depositId;
    private final String orderId;
    private final String paymentKey;
    private final BigDecimal amount;
    private final String status;
    private final LocalDateTime requestedAt;
    private final LocalDateTime approvedAt;

    public DepositHistoryResponse(DepositHistory depositHistory) {
        this.depositId = depositHistory.getId();
        this.orderId = depositHistory.getOrderId();
        this.paymentKey = depositHistory.getPaymentKey();
        this.amount = depositHistory.getAmount();
        this.status = depositHistory.getStatus();
        this.requestedAt = depositHistory.getRequestedAt();
        this.approvedAt = depositHistory.getApprovedAt();
    }
}