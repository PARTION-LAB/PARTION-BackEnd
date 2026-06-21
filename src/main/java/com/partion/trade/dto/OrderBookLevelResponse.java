package com.partion.trade.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class OrderBookLevelResponse {

    private BigDecimal price;
    private Long quantity;
    private Long orders;
}