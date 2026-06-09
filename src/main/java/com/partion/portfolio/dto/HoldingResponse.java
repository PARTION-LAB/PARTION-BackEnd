package com.partion.portfolio.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class HoldingResponse {

    private Long holdingId;
    private Long productId;
    private String category;
    private String productName;
    private Long quantity;
    private Long lockedQuantity;
    private Long availableQuantity;
    private BigDecimal averagePrice;
    private BigDecimal currentPrice;
    private BigDecimal valuationAmount;
    private BigDecimal expectedAnnualDividend;
    private BigDecimal expectedYield;
    private BigDecimal profitRate;
}