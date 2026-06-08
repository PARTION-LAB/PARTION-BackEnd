package com.partion.product.dto;

import com.partion.product.domain.Product;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
public class ProductDetailResponse {

    private final Long productId;
    private final Long issuerMemberId;
    private final String category;
    private final String name;
    private final String summary;
    private final String description;
    private final String imageUrl;
    private final String extraInfo;
    private final BigDecimal targetAmount;
    private final BigDecimal currentAmount;
    private final BigDecimal tokenPrice;
    private final Long totalTokenQuantity;
    private final Long fundedTokenQuantity;
    private final BigDecimal expectedYield;
    private final LocalDate deadline;
    private final BigDecimal fundingRate;
    private final String status;
    private final LocalDateTime createdAt;

    public ProductDetailResponse(Product product) {
        this.productId = product.getId();
        this.issuerMemberId = product.getIssuerMemberId();
        this.category = product.getCategory();
        this.name = product.getName();
        this.summary = product.getSummary();
        this.description = product.getDescription();
        this.imageUrl = product.getImageUrl();
        this.extraInfo = product.getExtraInfo();
        this.targetAmount = product.getTargetAmount();
        this.currentAmount = product.getCurrentAmount();
        this.tokenPrice = product.getTokenPrice();
        this.totalTokenQuantity = product.getTotalTokenQuantity();
        this.fundedTokenQuantity = product.getFundedTokenQuantity();
        this.expectedYield = product.getExpectedYield();
        this.deadline = product.getDeadline();
        this.fundingRate = calculateFundingRate(product);
        this.status = product.getStatus();
        this.createdAt = product.getCreatedAt();
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