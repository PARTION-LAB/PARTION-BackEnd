package com.partion.portfolio.service;

import com.partion.global.exception.BusinessException;
import com.partion.global.exception.ErrorCode;
import com.partion.global.response.PageResponse;
import com.partion.portfolio.dto.HoldingResponse;
import com.partion.portfolio.dto.PortfolioSummaryResponse;
import com.partion.portfolio.mapper.HoldingMapper;
import com.partion.trade.service.CurrentPriceCacheService;
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
    private final CurrentPriceCacheService currentPriceCacheService;

    public PageResponse<HoldingResponse> getMyHoldings(Long memberId, int page, int size) {
        validatePageRequest(page, size);

        int offset = page * size;

        List<HoldingResponse> content =
                holdingMapper.findMyHoldings(memberId, size, offset);

        content.forEach(this::applyCachedCurrentPrice);

        long totalElements = holdingMapper.countMyHoldings(memberId);

        return new PageResponse<>(content, page, size, totalElements);
    }

    private void applyCachedCurrentPrice(HoldingResponse holding) {
        currentPriceCacheService.getCurrentPrice(holding.getProductId())
                .ifPresent(holding::applyCurrentPrice);
    }

    private void validatePageRequest(int page, int size) {
        if (page < 0 || size <= 0 || size > 100) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    public PortfolioSummaryResponse getSummary(Long memberId) {
        Wallet wallet = walletMapper.findByMemberId(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.WALLET_NOT_FOUND));

        List<HoldingResponse> holdings = holdingMapper.findAllMyHoldings(memberId);
        holdings.forEach(this::applyCachedCurrentPrice);

        BigDecimal tokenValuationAmount = holdings.stream()
                .map(HoldingResponse::getValuationAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal expectedAnnualDividend = holdings.stream()
                .map(HoldingResponse::getExpectedAnnualDividend)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new PortfolioSummaryResponse(
                tokenValuationAmount,
                wallet.getAvailableBalance(),
                wallet.getLockedBalance(),
                expectedAnnualDividend
        );
    }
}