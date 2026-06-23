package com.partion.matching.event;

import java.math.BigDecimal;

public record TradeExecutedEvent(
        String eventType,
        String eventId,
        Long productId,
        Long buyOrderId,
        Long sellOrderId,
        BigDecimal price,
        Long quantity,
        Long occurredAt
) {
}