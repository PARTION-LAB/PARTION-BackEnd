package com.partion.order.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class MyOrderResponse {

    private Long orderId;
    private Long productId;
    private String productName;
    private String category;
    private String type;
    private String orderMethod;
    private BigDecimal price;
    private Long quantity;
    private Long remainingQuantity;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}