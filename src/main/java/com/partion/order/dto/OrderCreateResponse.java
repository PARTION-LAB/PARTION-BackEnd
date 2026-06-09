package com.partion.order.dto;

import com.partion.order.domain.Order;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
public class OrderCreateResponse {

    private final Long orderId;
    private final Long productId;
    private final String type;
    private final String orderMethod;
    private final BigDecimal price;
    private final Long quantity;
    private final Long remainingQuantity;
    private final String status;
    private final LocalDateTime createdAt;

    public OrderCreateResponse(Order order) {
        this.orderId = order.getId();
        this.productId = order.getProductId();
        this.type = order.getType();
        this.orderMethod = order.getOrderMethod();
        this.price = order.getPrice();
        this.quantity = order.getQuantity();
        this.remainingQuantity = order.getRemainingQuantity();
        this.status = order.getStatus();
        this.createdAt = order.getCreatedAt();
    }
}