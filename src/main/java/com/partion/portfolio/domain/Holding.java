package com.partion.portfolio.domain;

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
public class Holding {

    private Long id;
    private Long memberId;
    private Long productId;
    private Long quantity;
    private Long lockedQuantity;
    private BigDecimal averagePrice;
    private LocalDateTime updatedAt;
}