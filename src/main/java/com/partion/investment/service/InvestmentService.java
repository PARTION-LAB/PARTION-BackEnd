package com.partion.investment.service;

import com.partion.global.exception.BusinessException;
import com.partion.global.exception.ErrorCode;
import com.partion.global.response.PageResponse;
import com.partion.investment.domain.Investment;
import com.partion.investment.dto.CreateInvestmentRequest;
import com.partion.investment.dto.InvestmentCreateResponse;
import com.partion.investment.mapper.InvestmentMapper;
import com.partion.portfolio.domain.Holding;
import com.partion.portfolio.mapper.HoldingMapper;
import com.partion.product.domain.Product;
import com.partion.product.dto.ProductDetailResponse;
import com.partion.product.dto.ProductListResponse;
import com.partion.product.mapper.ProductMapper;
import com.partion.wallet.domain.Wallet;
import com.partion.wallet.domain.WalletTransaction;
import com.partion.wallet.mapper.WalletMapper;
import com.partion.wallet.mapper.WalletTransactionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class InvestmentService {

    private static final String FUNDING_STATUS = "FUNDING";

    private final ProductMapper productMapper;
    private final InvestmentMapper investmentMapper;
    private final WalletMapper walletMapper;
    private final WalletTransactionMapper walletTransactionMapper;
    private final HoldingMapper holdingMapper;

    public PageResponse<ProductListResponse> getFundingProducts(
            String category,
            String keyword,
            int page,
            int size
    ) {
        validatePageRequest(page, size);

        int offset = page * size;

        List<ProductListResponse> content = productMapper
                .findAllByStatus(FUNDING_STATUS, category, keyword, size, offset)
                .stream()
                .map(ProductListResponse::new)
                .toList();

        long totalElements = productMapper.countAllByStatus(FUNDING_STATUS, category, keyword);

        return new PageResponse<>(content, page, size, totalElements);
    }

    private void validatePageRequest(int page, int size) {
        if (page < 0 || size <= 0 || size > 100) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    public ProductDetailResponse getFundingProductDetail(Long productId) {
        Product product = productMapper.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        if (!FUNDING_STATUS.equals(product.getStatus())) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FUNDING);
        }

        return new ProductDetailResponse(product);
    }

    @Transactional
    public InvestmentCreateResponse invest(Long memberId, CreateInvestmentRequest request) {
        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new BusinessException(ErrorCode.INVALID_INVESTMENT_QUANTITY);
        }

        Product product = productMapper.findByIdForUpdate(request.getProductId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        if (!FUNDING_STATUS.equals(product.getStatus())) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FUNDING);
        }

        if (product.getDeadline().isBefore(LocalDate.now())) {
            throw new BusinessException(ErrorCode.PRODUCT_FUNDING_CLOSED);
        }

        long remainingTokenQuantity =
                product.getTotalTokenQuantity() - product.getFundedTokenQuantity();

        if (request.getQuantity() > remainingTokenQuantity) {
            throw new BusinessException(ErrorCode.PRODUCT_TOKEN_NOT_ENOUGH);
        }

        BigDecimal totalAmount = product.getTokenPrice()
                .multiply(BigDecimal.valueOf(request.getQuantity()));

        Wallet wallet = walletMapper.findByMemberIdForUpdate(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.WALLET_NOT_FOUND));

        if (wallet.getAvailableBalance().compareTo(totalAmount) < 0) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_BALANCE);
        }

        Investment investment = Investment.builder()
                .memberId(memberId)
                .productId(product.getId())
                .quantity(request.getQuantity())
                .pricePerToken(product.getTokenPrice())
                .totalAmount(totalAmount)
                .build();

        investmentMapper.insert(investment);

        BigDecimal updatedAvailableBalance =
                wallet.getAvailableBalance().subtract(totalAmount);

        Wallet updatedWallet = Wallet.builder()
                .id(wallet.getId())
                .memberId(wallet.getMemberId())
                .availableBalance(updatedAvailableBalance)
                .lockedBalance(wallet.getLockedBalance())
                .build();

        walletMapper.updateBalance(updatedWallet);

        WalletTransaction walletTransaction = WalletTransaction.builder()
                .walletId(wallet.getId())
                .type("INVEST")
                .amount(totalAmount.negate())
                .availableBalanceAfter(updatedAvailableBalance)
                .lockedBalanceAfter(wallet.getLockedBalance())
                .referenceType("INVESTMENT")
                .referenceId(investment.getId())
                .build();

        walletTransactionMapper.insert(walletTransaction);

        long updatedFundedTokenQuantity =
                product.getFundedTokenQuantity() + request.getQuantity();

        BigDecimal updatedCurrentAmount =
                product.getCurrentAmount().add(totalAmount);

        String updatedStatus = updatedFundedTokenQuantity == product.getTotalTokenQuantity()
                ? "TRADING"
                : "FUNDING";

        Product updatedProduct = Product.builder()
                .id(product.getId())
                .currentAmount(updatedCurrentAmount)
                .fundedTokenQuantity(updatedFundedTokenQuantity)
                .status(updatedStatus)
                .build();

        productMapper.updateFunding(updatedProduct);

        upsertHolding(memberId, product.getId(), request.getQuantity(), product.getTokenPrice());

        return new InvestmentCreateResponse(investment);
    }

    private void upsertHolding(
            Long memberId,
            Long productId,
            Long investQuantity,
            BigDecimal investPrice
    ) {
        Holding holding = holdingMapper
                .findByMemberIdAndProductIdForUpdate(memberId, productId)
                .orElse(null);

        if (holding == null) {
            insertHolding(memberId, productId, investQuantity, investPrice);
            return;
        }

        updateHolding(holding, investQuantity, investPrice);
    }

    private void insertHolding(
            Long memberId,
            Long productId,
            Long quantity,
            BigDecimal averagePrice
    ) {
        Holding holding = Holding.builder()
                .memberId(memberId)
                .productId(productId)
                .quantity(quantity)
                .lockedQuantity(0L)
                .averagePrice(averagePrice)
                .build();

        holdingMapper.insert(holding);
    }

    private void updateHolding(
            Holding holding,
            Long investQuantity,
            BigDecimal investPrice
    ) {
        Long oldQuantity = holding.getQuantity();
        BigDecimal oldAveragePrice = holding.getAveragePrice();

        Long newQuantity = oldQuantity + investQuantity;

        BigDecimal oldTotalAmount = oldAveragePrice.multiply(BigDecimal.valueOf(oldQuantity));
        BigDecimal newInvestAmount = investPrice.multiply(BigDecimal.valueOf(investQuantity));

        BigDecimal newAveragePrice = oldTotalAmount
                .add(newInvestAmount)
                .divide(BigDecimal.valueOf(newQuantity), 2, RoundingMode.HALF_UP);

        Holding updatedHolding = Holding.builder()
                .id(holding.getId())
                .memberId(holding.getMemberId())
                .productId(holding.getProductId())
                .quantity(newQuantity)
                .lockedQuantity(holding.getLockedQuantity())
                .averagePrice(newAveragePrice)
                .build();

        holdingMapper.update(updatedHolding);
    }
}