package com.partion.payment.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public class DepositConfirmResponse {

    private final Long depositId;
    private final String orderId;
    private final String paymentKey;
    private final BigDecimal amount;
    private final String status;
    private final LocalDateTime approvedAt;

    public DepositConfirmResponse(
            Long depositId,
            String orderId,
            String paymentKey,
            BigDecimal amount,
            String status,
            LocalDateTime approvedAt
    ) {
        this.depositId = depositId;
        this.orderId = orderId;
        this.paymentKey = paymentKey;
        this.amount = amount;
        this.status = status;
        this.approvedAt = approvedAt;
    }
}