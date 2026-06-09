package com.partion.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class CreateOrderRequest {

    @NotNull(message = "상품 ID는 필수입니다.")
    private Long productId;

    @NotBlank(message = "주문 유형은 필수입니다.")
    private String type; // BUY, SELL

    @NotBlank(message = "주문 방식은 필수입니다.")
    private String orderMethod; // LIMIT

    @NotNull(message = "주문 가격은 필수입니다.")
    @Positive(message = "주문 가격은 0보다 커야 합니다.")
    private BigDecimal price;

    @NotNull(message = "주문 수량은 필수입니다.")
    @Positive(message = "주문 수량은 0보다 커야 합니다.")
    private Long quantity;
}