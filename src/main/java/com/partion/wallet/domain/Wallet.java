package com.partion.wallet.domain;

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
public class Wallet {
    private Long id;
    private Long memberId;
    private BigDecimal availableBalance;
    private BigDecimal lockedBalance;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
