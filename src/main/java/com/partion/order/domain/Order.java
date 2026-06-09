package com.partion.order.domain;

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
public class Order {

    private Long id;
    private Long memberId;
    private Long productId;
    private String type;
    private String orderMethod;
    private BigDecimal price;
    private Long quantity;
    private Long remainingQuantity;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}