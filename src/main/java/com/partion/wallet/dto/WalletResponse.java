package com.partion.wallet.dto;

import com.partion.wallet.domain.Wallet;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class WalletResponse {

    private final Long walletId;
    private final Long memberId;
    private final BigDecimal availableBalance;
    private final BigDecimal lockedBalance;
    private final BigDecimal totalBalance;

    public WalletResponse(Wallet wallet) {
        this.walletId = wallet.getId();
        this.memberId = wallet.getMemberId();
        this.availableBalance = wallet.getAvailableBalance();
        this.lockedBalance = wallet.getLockedBalance();
        this.totalBalance = wallet.getAvailableBalance().add(wallet.getLockedBalance());
    }
}