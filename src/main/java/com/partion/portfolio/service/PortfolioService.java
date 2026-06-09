package com.partion.portfolio.service;

import com.partion.global.exception.BusinessException;
import com.partion.global.exception.ErrorCode;
import com.partion.global.response.PageResponse;
import com.partion.portfolio.dto.HoldingResponse;
import com.partion.portfolio.dto.PortfolioSummaryResponse;
import com.partion.portfolio.mapper.HoldingMapper;
import com.partion.wallet.domain.Wallet;
import com.partion.wallet.mapper.WalletMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class PortfolioService {

    private final HoldingMapper holdingMapper;
    private final WalletMapper walletMapper;

    public PageResponse<HoldingResponse> getMyHoldings(Long memberId, int page, int size) {
        validatePageRequest(page, size);

        int offset = page * size;

        List<HoldingResponse> content =
                holdingMapper.findMyHoldings(memberId, size, offset);

        long totalElements = holdingMapper.countMyHoldings(memberId);

        return new PageResponse<>(content, page, size, totalElements);
    }

    private void validatePageRequest(int page, int size) {
        if (page < 0 || size <= 0 || size > 100) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    public PortfolioSummaryResponse getSummary(Long memberId) {
        Wallet wallet = walletMapper.findByMemberId(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.WALLET_NOT_FOUND));

        BigDecimal tokenValuationAmount =
                holdingMapper.sumTokenValuationAmount(memberId);

        BigDecimal expectedAnnualDividend =
                holdingMapper.sumExpectedAnnualDividend(memberId);

        return new PortfolioSummaryResponse(
                tokenValuationAmount,
                wallet.getAvailableBalance(),
                wallet.getLockedBalance(),
                expectedAnnualDividend
        );
    }
}