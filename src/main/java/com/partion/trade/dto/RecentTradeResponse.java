package com.partion.trade.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class RecentTradeResponse {

    private Long tradeId;
    private Long productId;
    private String symbol;
    private BigDecimal price;
    private Long quantity;
    private LocalDateTime tradedAt;
}