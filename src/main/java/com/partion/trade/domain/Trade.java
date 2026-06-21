package com.partion.trade.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Trade {

    private Long id;
    private Long productId;
    private Long buyOrderId;
    private Long sellOrderId;
    private Long buyerMemberId;
    private Long sellerMemberId;
    private BigDecimal price;
    private Long quantity;
    private LocalDateTime tradedAt;
}