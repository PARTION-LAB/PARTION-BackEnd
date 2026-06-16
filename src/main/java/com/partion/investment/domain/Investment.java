package com.partion.investment.domain;

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
public class Investment {

    private Long id;
    private Long memberId;
    private Long productId;
    private Long quantity;
    private BigDecimal pricePerToken;
    private BigDecimal totalAmount;
    private String status;
    private LocalDateTime refundedAt;
    private LocalDateTime createdAt;
}