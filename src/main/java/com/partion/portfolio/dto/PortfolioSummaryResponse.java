package com.partion.portfolio.dto;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class PortfolioSummaryResponse {

    private final BigDecimal totalAsset;
    private final BigDecimal tokenValuationAmount;
    private final BigDecimal availableBalance;
    private final BigDecimal lockedBalance;
    private final BigDecimal expectedAnnualDividend;

    public PortfolioSummaryResponse(
            BigDecimal tokenValuationAmount,
            BigDecimal availableBalance,
            BigDecimal lockedBalance,
            BigDecimal expectedAnnualDividend
    ) {
        this.tokenValuationAmount = tokenValuationAmount;
        this.availableBalance = availableBalance;
        this.lockedBalance = lockedBalance;
        this.expectedAnnualDividend = expectedAnnualDividend;
        this.totalAsset = availableBalance
                .add(lockedBalance)
                .add(tokenValuationAmount);
    }
}