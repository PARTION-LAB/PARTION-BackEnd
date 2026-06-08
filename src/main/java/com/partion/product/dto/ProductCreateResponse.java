package com.partion.product.dto;

import com.partion.product.domain.Product;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class ProductCreateResponse {

    private final Long productId;
    private final String category;
    private final String name;
    private final String summary;
    private final String imageUrl;
    private final BigDecimal targetAmount;
    private final BigDecimal tokenPrice;
    private final Long totalTokenQuantity;
    private final String status;

    public ProductCreateResponse(Product product) {
        this.productId = product.getId();
        this.category = product.getCategory();
        this.name = product.getName();
        this.summary = product.getSummary();
        this.imageUrl = product.getImageUrl();
        this.targetAmount = product.getTargetAmount();
        this.tokenPrice = product.getTokenPrice();
        this.totalTokenQuantity = product.getTotalTokenQuantity();
        this.status = product.getStatus();
    }
}