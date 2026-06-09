package com.partion.investment.dto;

import com.partion.investment.domain.Investment;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
public class InvestmentCreateResponse {

    private final Long investmentId;
    private final Long productId;
    private final Long quantity;
    private final BigDecimal pricePerToken;
    private final BigDecimal totalAmount;
    private final LocalDateTime createdAt;

    public InvestmentCreateResponse(Investment investment) {
        this.investmentId = investment.getId();
        this.productId = investment.getProductId();
        this.quantity = investment.getQuantity();
        this.pricePerToken = investment.getPricePerToken();
        this.totalAmount = investment.getTotalAmount();
        this.createdAt = investment.getCreatedAt();
    }
}