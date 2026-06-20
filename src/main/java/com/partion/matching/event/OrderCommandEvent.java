package com.partion.matching.event;

import com.partion.order.domain.Order;

import java.math.BigDecimal;

public record OrderCommandEvent(
        String commandType,
        Long orderId,
        Long memberId,
        Long productId,
        String side,
        BigDecimal price,
        Long quantity
) {

    public static OrderCommandEvent create(Order order) {
        return new OrderCommandEvent(
                "CREATE",
                order.getId(),
                order.getMemberId(),
                order.getProductId(),
                order.getType(),
                order.getPrice(),
                order.getQuantity()
        );
    }

    public static OrderCommandEvent cancel(Order order) {
        return new OrderCommandEvent(
                "CANCEL",
                order.getId(),
                order.getMemberId(),
                order.getProductId(),
                order.getType(),
                order.getPrice(),
                order.getRemainingQuantity()
        );
    }
}