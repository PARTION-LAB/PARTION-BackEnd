package com.partion.investment.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;

@Getter
public class CreateInvestmentRequest {

    @NotNull(message = "상품 ID는 필수입니다.")
    private Long productId;

    @NotNull(message = "투자 수량은 필수입니다.")
    @Positive(message = "투자 수량은 0보다 커야 합니다.")
    private Long quantity;
}