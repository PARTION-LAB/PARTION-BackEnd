package com.partion.product.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    private Long id;
    private Long issuerMemberId;
    private String category;
    private String name;
    private String summary;
    private String description;
    private String imageUrl;
    private String extraInfo;
    private BigDecimal targetAmount;
    private BigDecimal currentAmount;
    private BigDecimal tokenPrice;
    private Long totalTokenQuantity;
    private Long fundedTokenQuantity;
    private BigDecimal expectedYield;
    private LocalDate deadline;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}