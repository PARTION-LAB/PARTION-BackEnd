package com.partion.wallet.service;

import com.partion.global.exception.BusinessException;
import com.partion.global.exception.ErrorCode;
import com.partion.global.response.PageResponse;
import com.partion.wallet.domain.Wallet;
import com.partion.wallet.dto.WalletResponse;
import com.partion.wallet.dto.WalletTransactionResponse;
import com.partion.wallet.mapper.WalletMapper;
import com.partion.wallet.mapper.WalletTransactionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class WalletService {

    private final WalletMapper walletMapper;
    private final WalletTransactionMapper walletTransactionMapper;

    public WalletResponse getMyWallet(Long memberId) {
        Wallet wallet = walletMapper.findByMemberId(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.WALLET_NOT_FOUND));

        return new WalletResponse(wallet);
    }

    public PageResponse<WalletTransactionResponse> getMyWalletTransactions(
            Long memberId,
            int page,
            int size
    ) {
        validatePageRequest(page, size);

        Wallet wallet = walletMapper.findByMemberId(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.WALLET_NOT_FOUND));

        int offset = page * size;

        List<WalletTransactionResponse> content = walletTransactionMapper
                .findByWalletId(wallet.getId(), size, offset)
                .stream()
                .map(WalletTransactionResponse::new)
                .toList();

        long totalElements = walletTransactionMapper.countByWalletId(wallet.getId());

        return new PageResponse<>(content, page, size, totalElements);
    }

    private void validatePageRequest(int page, int size) {
        if (page < 0 || size <= 0 || size > 100) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }
}
