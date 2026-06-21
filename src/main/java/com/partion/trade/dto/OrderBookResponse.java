package com.partion.trade.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class OrderBookResponse {

    private final Long productId;
    private final List<OrderBookLevelResponse> asks;
    private final List<OrderBookLevelResponse> bids;

    public OrderBookResponse(
            Long productId,
            List<OrderBookLevelResponse> asks,
            List<OrderBookLevelResponse> bids
    ) {
        this.productId = productId;
        this.asks = asks;
        this.bids = bids;
    }
}