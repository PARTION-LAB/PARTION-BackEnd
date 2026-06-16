package com.partion.batch.service;

import com.partion.global.exception.BusinessException;
import com.partion.global.exception.ErrorCode;
import com.partion.investment.domain.Investment;
import com.partion.investment.mapper.InvestmentMapper;
import com.partion.portfolio.domain.Holding;
import com.partion.portfolio.mapper.HoldingMapper;
import com.partion.product.domain.Product;
import com.partion.product.mapper.ProductMapper;
import com.partion.wallet.domain.Wallet;
import com.partion.wallet.domain.WalletTransaction;
import com.partion.wallet.mapper.WalletMapper;
import com.partion.wallet.mapper.WalletTransactionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FundingRefundBatchService {

    private final ProductMapper productMapper;
    private final InvestmentMapper investmentMapper;
    private final WalletMapper walletMapper;
    private final HoldingMapper holdingMapper;
    private final WalletTransactionMapper walletTransactionMapper;

    @Transactional
    public int closeExpiredFundingProductsAndRefund() {
        List<Product> products = productMapper.findExpiredFailedFundingProducts(LocalDate.now());

        int refundedInvestmentCount = 0;

        for (Product product : products) {
            refundedInvestmentCount += refundProduct(product.getId());
        }

        return refundedInvestmentCount;
    }

    private int refundProduct(Long productId) {
        Product product = productMapper.findByIdForUpdate(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        // 방어 검증
        if (!"FUNDING".equals(product.getStatus())) {
            return 0;
        }

        if (!product.getDeadline().isBefore(LocalDate.now())) {
            return 0;
        }

        if (product.getCurrentAmount().compareTo(product.getTargetAmount()) >= 0) {
            return 0;
        }

        List<Investment> investments =
                investmentMapper.findCompletedByProductIdForUpdate(productId);

        for (Investment investment : investments) {
            refundInvestment(investment);
        }

        productMapper.closeProduct(productId);

        return investments.size();
    }

    private void refundInvestment(Investment investment) {
        Wallet wallet = walletMapper.findByMemberIdForUpdate(investment.getMemberId())
                .orElseThrow(() -> new BusinessException(ErrorCode.WALLET_NOT_FOUND));

        BigDecimal updatedAvailableBalance =
                wallet.getAvailableBalance().add(investment.getTotalAmount());

        Wallet updatedWallet = Wallet.builder()
                .id(wallet.getId())
                .memberId(wallet.getMemberId())
                .availableBalance(updatedAvailableBalance)
                .lockedBalance(wallet.getLockedBalance())
                .build();

        walletMapper.updateBalance(updatedWallet);

        Holding holding = holdingMapper.findByMemberIdAndProductIdForUpdate(
                        investment.getMemberId(),
                        investment.getProductId()
                )
                .orElseThrow(() -> new BusinessException(ErrorCode.HOLDING_NOT_FOUND));

        Holding updatedHolding = Holding.builder()
                .id(holding.getId())
                .memberId(holding.getMemberId())
                .productId(holding.getProductId())
                .quantity(holding.getQuantity() - investment.getQuantity())
                .lockedQuantity(holding.getLockedQuantity())
                .averagePrice(holding.getAveragePrice())
                .build();

        holdingMapper.update(updatedHolding);

        WalletTransaction walletTransaction = WalletTransaction.builder()
                .walletId(wallet.getId())
                .type("REFUND")
                .amount(investment.getTotalAmount())
                .availableBalanceAfter(updatedAvailableBalance)
                .lockedBalanceAfter(wallet.getLockedBalance())
                .referenceType("INVESTMENT")
                .referenceId(investment.getId())
                .build();

        walletTransactionMapper.insert(walletTransaction);

        investmentMapper.updateStatusToRefunded(investment.getId());
    }
}
