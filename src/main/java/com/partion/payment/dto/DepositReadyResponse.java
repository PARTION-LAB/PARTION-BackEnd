package com.partion.payment.dto;

import java.math.BigDecimal;

import lombok.Getter;

@Getter
public class DepositReadyResponse {

    private final Long depositId;
    private final String orderId;
    private final BigDecimal amount;
    private final String status;

    public DepositReadyResponse(Long depositId, String orderId, BigDecimal amount, String status) {
        this.depositId = depositId;
        this.orderId = orderId;
        this.amount = amount;
        this.status = status;
    }
}