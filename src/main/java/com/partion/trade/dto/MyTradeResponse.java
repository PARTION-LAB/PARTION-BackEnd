package com.partion.trade.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class MyTradeResponse {

    private Long tradeId;
    private Long productId;
    private String productName;
    private String type;
    private BigDecimal price;
    private Long quantity;
    private BigDecimal totalAmount;
    private LocalDateTime tradedAt;
}