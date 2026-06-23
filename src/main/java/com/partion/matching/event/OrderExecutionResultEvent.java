package com.partion.matching.event;

public record OrderExecutionResultEvent(
        String eventType,
        String eventId,
        Long orderId,
        Long productId,
        String side,
        String orderMethod,
        Long requestedQuantity,
        Long filledQuantity,
        Long canceledQuantity,
        Long remainingQuantity,
        String finalStatus,
        Long occurredAt
) {
}