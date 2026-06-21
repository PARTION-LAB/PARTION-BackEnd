package com.partion.matching.service;

import com.partion.global.exception.BusinessException;
import com.partion.global.exception.ErrorCode;
import com.partion.matching.config.KafkaTopicConfig;
import com.partion.matching.event.TradeExecutedEvent;
import com.partion.order.domain.Order;
import com.partion.order.mapper.OrderMapper;
import com.partion.portfolio.domain.Holding;
import com.partion.portfolio.mapper.HoldingMapper;
import com.partion.trade.domain.Trade;
import com.partion.trade.mapper.TradeMapper;
import com.partion.wallet.domain.Wallet;
import com.partion.wallet.domain.WalletTransaction;
import com.partion.wallet.mapper.WalletMapper;
import com.partion.wallet.mapper.WalletTransactionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@RequiredArgsConstructor
@Service
public class TradeSettlementService {

    private static final String FILLED = "FILLED";
    private static final String PARTIALLY_FILLED = "PARTIALLY_FILLED";
    private static final String TRADE = "TRADE";

    private final OrderMapper orderMapper;
    private final TradeMapper tradeMapper;
    private final WalletMapper walletMapper;
    private final WalletTransactionMapper walletTransactionMapper;
    private final HoldingMapper holdingMapper;

    @KafkaListener(
            topics = KafkaTopicConfig.TRADE_EVENTS,
            groupId = "partion-settlement"
    )
    @Transactional
    public void handleTradeExecuted(TradeExecutedEvent event) {
        Order buyOrder = orderMapper.findByIdForUpdate(event.buyOrderId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        Order sellOrder = orderMapper.findByIdForUpdate(event.sellOrderId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        Trade trade = Trade.builder()
                .productId(event.productId())
                .buyOrderId(event.buyOrderId())
                .sellOrderId(event.sellOrderId())
                .buyerMemberId(buyOrder.getMemberId())
                .sellerMemberId(sellOrder.getMemberId())
                .price(event.price())
                .quantity(event.quantity())
                .build();

        tradeMapper.insert(trade);

        updateOrderAfterTrade(buyOrder, event.quantity());
        updateOrderAfterTrade(sellOrder, event.quantity());

        settleBuyer(buyOrder, event, trade);
        settleSeller(sellOrder, event, trade);
    }

    private void updateOrderAfterTrade(Order order, Long tradedQuantity) {
        long remainingQuantity = order.getRemainingQuantity() - tradedQuantity;

        String status = remainingQuantity == 0 ? FILLED : PARTIALLY_FILLED;

        Order updatedOrder = Order.builder()
                .id(order.getId())
                .remainingQuantity(remainingQuantity)
                .status(status)
                .build();

        orderMapper.updateRemainingQuantityAndStatus(updatedOrder);
    }

    private void settleBuyer(Order buyOrder, TradeExecutedEvent event, Trade trade) {
        Wallet buyerWallet = walletMapper.findByMemberIdForUpdate(buyOrder.getMemberId())
                .orElseThrow(() -> new BusinessException(ErrorCode.WALLET_NOT_FOUND));

        BigDecimal lockedAmount = buyOrder.getPrice()
                .multiply(BigDecimal.valueOf(event.quantity()));

        BigDecimal actualTradeAmount = event.price()
                .multiply(BigDecimal.valueOf(event.quantity()));

        BigDecimal refundAmount = lockedAmount.subtract(actualTradeAmount);

        BigDecimal updatedAvailableBalance = buyerWallet.getAvailableBalance().add(refundAmount);
        BigDecimal updatedLockedBalance = buyerWallet.getLockedBalance().subtract(lockedAmount);

        Wallet updatedWallet = Wallet.builder()
                .id(buyerWallet.getId())
                .memberId(buyerWallet.getMemberId())
                .availableBalance(updatedAvailableBalance)
                .lockedBalance(updatedLockedBalance)
                .build();

        walletMapper.updateBalance(updatedWallet);

        WalletTransaction walletTransaction = WalletTransaction.builder()
                .walletId(buyerWallet.getId())
                .type("BUY")
                .amount(actualTradeAmount.negate())
                .availableBalanceAfter(updatedAvailableBalance)
                .lockedBalanceAfter(updatedLockedBalance)
                .referenceType(TRADE)
                .referenceId(trade.getId())
                .build();

        walletTransactionMapper.insert(walletTransaction);

        increaseBuyerHolding(buyOrder, event);
    }

    private void settleSeller(Order sellOrder, TradeExecutedEvent event, Trade trade) {
        Wallet sellerWallet = walletMapper.findByMemberIdForUpdate(sellOrder.getMemberId())
                .orElseThrow(() -> new BusinessException(ErrorCode.WALLET_NOT_FOUND));

        BigDecimal tradeAmount = event.price()
                .multiply(BigDecimal.valueOf(event.quantity()));

        BigDecimal updatedAvailableBalance = sellerWallet.getAvailableBalance().add(tradeAmount);

        Wallet updatedWallet = Wallet.builder()
                .id(sellerWallet.getId())
                .memberId(sellerWallet.getMemberId())
                .availableBalance(updatedAvailableBalance)
                .lockedBalance(sellerWallet.getLockedBalance())
                .build();

        walletMapper.updateBalance(updatedWallet);

        WalletTransaction walletTransaction = WalletTransaction.builder()
                .walletId(sellerWallet.getId())
                .type("SELL")
                .amount(tradeAmount)
                .availableBalanceAfter(updatedAvailableBalance)
                .lockedBalanceAfter(sellerWallet.getLockedBalance())
                .referenceType(TRADE)
                .referenceId(trade.getId())
                .build();

        walletTransactionMapper.insert(walletTransaction);

        decreaseSellerHolding(sellOrder, event);
    }

    private void increaseBuyerHolding(Order buyOrder, TradeExecutedEvent event) {
        Holding holding = holdingMapper
                .findByMemberIdAndProductIdForUpdate(buyOrder.getMemberId(), event.productId())
                .orElse(null);

        if (holding == null) {
            Holding newHolding = Holding.builder()
                    .memberId(buyOrder.getMemberId())
                    .productId(event.productId())
                    .quantity(event.quantity())
                    .lockedQuantity(0L)
                    .averagePrice(event.price())
                    .build();

            holdingMapper.insert(newHolding);
            return;
        }

        BigDecimal oldTotalAmount = holding.getAveragePrice()
                .multiply(BigDecimal.valueOf(holding.getQuantity()));

        BigDecimal newTradeAmount = event.price()
                .multiply(BigDecimal.valueOf(event.quantity()));

        long updatedQuantity = holding.getQuantity() + event.quantity();

        BigDecimal updatedAveragePrice = oldTotalAmount
                .add(newTradeAmount)
                .divide(BigDecimal.valueOf(updatedQuantity), 2, RoundingMode.HALF_UP);

        Holding updatedHolding = Holding.builder()
                .id(holding.getId())
                .quantity(updatedQuantity)
                .lockedQuantity(holding.getLockedQuantity())
                .averagePrice(updatedAveragePrice)
                .build();

        holdingMapper.update(updatedHolding);
    }

    private void decreaseSellerHolding(Order sellOrder, TradeExecutedEvent event) {
        Holding holding = holdingMapper
                .findByMemberIdAndProductIdForUpdate(sellOrder.getMemberId(), event.productId())
                .orElseThrow(() -> new BusinessException(ErrorCode.HOLDING_NOT_FOUND));

        Holding updatedHolding = Holding.builder()
                .id(holding.getId())
                .quantity(holding.getQuantity() - event.quantity())
                .lockedQuantity(holding.getLockedQuantity() - event.quantity())
                .averagePrice(holding.getAveragePrice())
                .build();

        holdingMapper.update(updatedHolding);
    }
}