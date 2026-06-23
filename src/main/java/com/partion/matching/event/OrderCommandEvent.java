package com.partion.matching.event;

import com.partion.order.domain.Order;

import java.math.BigDecimal;

public record OrderCommandEvent(
        String commandType,
        Long orderId,
        Long memberId,
        Long productId,
        String side,
        String orderMethod,
        BigDecimal price,
        Long quantity
) {

    public static OrderCommandEvent create(Order order) {
        return new OrderCommandEvent(
                "NEW_ORDER",
                order.getId(),
                order.getMemberId(),
                order.getProductId(),
                order.getType(),
                order.getOrderMethod(),
                order.getPrice(),
                order.getQuantity()
        );
    }

    public static OrderCommandEvent cancel(Order order) {
        return new OrderCommandEvent(
                "CANCEL_ORDER",
                order.getId(),
                order.getMemberId(),
                order.getProductId(),
                order.getType(),
                order.getOrderMethod(),
                order.getPrice(),
                order.getRemainingQuantity()
        );
    }

    public static OrderCommandEvent resync(Order order) {
        return new OrderCommandEvent(
                "NEW_ORDER",
                order.getId(),
                order.getMemberId(),
                order.getProductId(),
                order.getType(),
                order.getOrderMethod(),
                order.getPrice(),
                order.getRemainingQuantity()
        );
    }
}