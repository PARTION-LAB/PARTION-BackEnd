package com.partion.investment.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class MyInvestmentResponse {

    private Long investmentId;
    private Long productId;
    private String productName;
    private String category;
    private Long quantity;
    private BigDecimal pricePerToken;
    private BigDecimal totalAmount;
    private LocalDateTime createdAt;
}