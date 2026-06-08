package com.partion.payment.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
public class DepositReadyRequest {

    @NotNull(message = "충전 금액은 필수입니다.")
    private BigDecimal amount;
}