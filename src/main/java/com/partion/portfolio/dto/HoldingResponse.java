package com.partion.portfolio.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;

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

    public void applyCurrentPrice(BigDecimal currentPrice) {
        this.currentPrice = currentPrice;
        this.valuationAmount = currentPrice.multiply(BigDecimal.valueOf(quantity));
        this.expectedAnnualDividend = valuationAmount
                .multiply(expectedYield == null ? BigDecimal.ZERO : expectedYield)
                .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);

        if (averagePrice == null || BigDecimal.ZERO.compareTo(averagePrice) == 0) {
            this.profitRate = BigDecimal.ZERO;
            return;
        }

        this.profitRate = currentPrice
                .subtract(averagePrice)
                .divide(averagePrice, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }
}