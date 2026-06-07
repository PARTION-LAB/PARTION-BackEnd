package com.partion.wallet.service;

import com.partion.global.exception.BusinessException;
import com.partion.global.exception.ErrorCode;
import com.partion.wallet.domain.Wallet;
import com.partion.wallet.dto.WalletResponse;
import com.partion.wallet.mapper.WalletMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class WalletService {

    private final WalletMapper walletMapper;

    public WalletResponse getMyWallet(Long memberId) {
        Wallet wallet = walletMapper.findByMemberId(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.WALLET_NOT_FOUND));

        return new WalletResponse(wallet);
    }
}
