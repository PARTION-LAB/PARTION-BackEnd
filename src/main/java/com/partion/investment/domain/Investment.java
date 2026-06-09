package com.partion.investment.domain;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class Investment {

    private Long id;
    private Long memberId;
    private Long productId;
    private Long quantity;
    private BigDecimal pricePerToken;
    private BigDecimal totalAmount;
    private LocalDateTime createdAt;
}