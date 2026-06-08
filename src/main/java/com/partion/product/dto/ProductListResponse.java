package com.partion.product.dto;

import com.partion.product.domain.Product;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Getter
public class ProductListResponse {

    private final Long productId;
    private final String category;
    private final String name;
    private final String summary;
    private final String imageUrl;
    private final BigDecimal tokenPrice;
    private final BigDecimal expectedYield;
    private final LocalDate deadline;
    private final BigDecimal targetAmount;
    private final BigDecimal currentAmount;
    private final BigDecimal fundingRate;
    private final String status;

    public ProductListResponse(Product product) {
        this.productId = product.getId();
        this.category = product.getCategory();
        this.name = product.getName();
        this.summary = product.getSummary();
        this.imageUrl = product.getImageUrl();
        this.tokenPrice = product.getTokenPrice();
        this.expectedYield = product.getExpectedYield();
        this.deadline = product.getDeadline();
        this.targetAmount = product.getTargetAmount();
        this.currentAmount = product.getCurrentAmount();
        this.fundingRate = calculateFundingRate(product);
        this.status = product.getStatus();
    }

    private BigDecimal calculateFundingRate(Product product) {
        if (product.getTargetAmount() == null
                || product.getTargetAmount().compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        return product.getCurrentAmount()
                .multiply(BigDecimal.valueOf(100))
                .divide(product.getTargetAmount(), 2, RoundingMode.HALF_UP);
    }
}