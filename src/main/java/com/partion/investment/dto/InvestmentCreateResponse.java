package com.partion.investment.dto;

import com.partion.investment.domain.Investment;
import com.partion.product.domain.Product;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
public class InvestmentCreateResponse {

    private final Long investmentId;
    private final Long productId;
    private final Long quantity;
    private final BigDecimal pricePerToken;
    private final BigDecimal totalAmount;
    private final LocalDateTime createdAt;
    private final Long requestedQuantity;
    private final Long investedQuantity;
    private final Long unfilledQuantity;
    private final BigDecimal leftoverAmount;
    private final Boolean partialFilled;

    public InvestmentCreateResponse(Investment investment, Long requestedQuantity) {
        this.investmentId = investment.getId();
        this.productId = investment.getProductId();
        this.quantity = investment.getQuantity();
        this.pricePerToken = investment.getPricePerToken();
        this.totalAmount = investment.getTotalAmount();
        this.createdAt = investment.getCreatedAt();

        this.requestedQuantity = requestedQuantity;
        this.investedQuantity = investment.getQuantity();
        this.unfilledQuantity = requestedQuantity - investment.getQuantity();
        this.leftoverAmount = investment.getPricePerToken()
                .multiply(BigDecimal.valueOf(this.unfilledQuantity));
        this.partialFilled = this.unfilledQuantity > 0;
    }

    private InvestmentCreateResponse(
            Long investmentId,
            Long productId,
            Long quantity,
            BigDecimal pricePerToken,
            BigDecimal totalAmount,
            LocalDateTime createdAt,
            Long requestedQuantity,
            Long investedQuantity,
            Long unfilledQuantity,
            BigDecimal leftoverAmount,
            Boolean partialFilled
    ) {
        this.investmentId = investmentId;
        this.productId = productId;
        this.quantity = quantity;
        this.pricePerToken = pricePerToken;
        this.totalAmount = totalAmount;
        this.createdAt = createdAt;
        this.requestedQuantity = requestedQuantity;
        this.investedQuantity = investedQuantity;
        this.unfilledQuantity = unfilledQuantity;
        this.leftoverAmount = leftoverAmount;
        this.partialFilled = partialFilled;
    }

    public static InvestmentCreateResponse notInvested(Product product, Long requestedQuantity) {
        return new InvestmentCreateResponse(
                null,
                product.getId(),
                0L,
                product.getTokenPrice(),
                BigDecimal.ZERO,
                null,
                requestedQuantity,
                0L,
                requestedQuantity,
                product.getTokenPrice().multiply(BigDecimal.valueOf(requestedQuantity)),
                true
        );
    }
}