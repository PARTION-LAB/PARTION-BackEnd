package com.partion.matching.service;

import com.partion.matching.event.OrderExecutionResultEvent;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import com.partion.global.exception.BusinessException;
import com.partion.global.exception.ErrorCode;
import com.partion.ledger.event.LedgerEvent;
import com.partion.matching.config.KafkaTopicConfig;
import com.partion.matching.event.TradeExecutedEvent;
import com.partion.order.domain.Order;
import com.partion.order.mapper.OrderMapper;
import com.partion.portfolio.domain.Holding;
import com.partion.portfolio.mapper.HoldingMapper;
import com.partion.product.mapper.ProductMapper;
import com.partion.ledger.service.LedgerEventPublisher;
import com.partion.product.domain.Product;
import com.partion.trade.domain.Trade;
import com.partion.trade.mapper.TradeMapper;
import com.partion.trade.service.CurrentPriceCacheService;
import com.partion.wallet.domain.Wallet;
import com.partion.wallet.domain.WalletTransaction;
import com.partion.wallet.mapper.WalletMapper;
import com.partion.wallet.mapper.WalletTransactionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionSynchronization;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class TradeSettlementService {

    private static final String FILLED = "FILLED";
    private static final String PARTIALLY_FILLED = "PARTIALLY_FILLED";
    private static final String TRADE = "TRADE";
    private static final String OPEN = "OPEN";

    private final OrderMapper orderMapper;
    private final TradeMapper tradeMapper;
    private final WalletMapper walletMapper;
    private final WalletTransactionMapper walletTransactionMapper;
    private final HoldingMapper holdingMapper;
    private final CurrentPriceCacheService currentPriceCacheService;
    private final ProductMapper productMapper;
    private final ObjectMapper objectMapper;
    private final LedgerEventPublisher ledgerEventPublisher;

    private void handleTradeExecuted(TradeExecutedEvent event) {
        OrderPair orderPair = lockOrders(event.buyOrderId(), event.sellOrderId());
        Order buyOrder = orderPair.buyOrder();
        Order sellOrder = orderPair.sellOrder();

        if (!isExecutableOrder(buyOrder) || !isExecutableOrder(sellOrder)) {
            return;
        }

        long executableQuantity = Math.min(
                event.quantity(),
                Math.min(buyOrder.getRemainingQuantity(), sellOrder.getRemainingQuantity())
        );

        if (executableQuantity <= 0) {
            return;
        }

        Trade trade = Trade.builder()
                .productId(event.productId())
                .buyOrderId(event.buyOrderId())
                .sellOrderId(event.sellOrderId())
                .buyerMemberId(buyOrder.getMemberId())
                .sellerMemberId(sellOrder.getMemberId())
                .price(event.price())
                .quantity(executableQuantity)
                .build();

        tradeMapper.insert(trade);

        updateOrderAfterTrade(buyOrder, executableQuantity);
        updateOrderAfterTrade(sellOrder, executableQuantity);

        settleBuyer(buyOrder, event.price(), executableQuantity, trade);
        settleSeller(sellOrder, event.price(), executableQuantity, trade);

        saveCurrentPriceAfterCommit(trade.getProductId(), trade.getPrice());

        publishTradeSettledEvent(event, trade, buyOrder, sellOrder, executableQuantity);
    }

    @KafkaListener(
            topics = KafkaTopicConfig.TRADE_EVENTS,
            groupId = "partion-settlement"
    )
    @Transactional
    public void handleMatchingEvent(String payload) {
        String eventType = resolveEventType(payload);

        if ("ORDER_EXECUTION_RESULT".equals(eventType)) {
            OrderExecutionResultEvent event =
                    readPayload(payload, OrderExecutionResultEvent.class);
            handleOrderExecutionResult(event);
            return;
        }

        TradeExecutedEvent event =
                readPayload(payload, TradeExecutedEvent.class);
        handleTradeExecuted(event);
    }

    private String resolveEventType(String payload) {
        Map<String, Object> values = readPayload(
                payload,
                new TypeReference<Map<String, Object>>() {}
        );

        Object eventType = values.get("eventType");

        if (eventType == null) {
            return "TRADE_EXECUTED";
        }

        return String.valueOf(eventType);
    }

    private <T> T readPayload(String payload, TypeReference<T> typeReference) {
        try {
            return objectMapper.readValue(payload, typeReference);
        } catch (JacksonException exception) {
            throw new IllegalArgumentException("Invalid matching event payload.", exception);
        }
    }

    private <T> T readPayload(String payload, Class<T> type) {
        try {
            return objectMapper.readValue(payload, type);
        } catch (JacksonException exception) {
            throw new IllegalArgumentException("Invalid matching event payload.", exception);
        }
    }

    private void handleOrderExecutionResult(OrderExecutionResultEvent event) {
        Order order = orderMapper.findByIdForUpdate(event.orderId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        if (!"MARKET".equals(order.getOrderMethod())) {
            return;
        }

        if (!isExecutableOrder(order)) {
            return;
        }

        long cancelQuantity = Math.max(0, order.getRemainingQuantity());

        if (cancelQuantity > 0) {
            if ("BUY".equals(order.getType())) {
                unlockBuyRemainder(order, cancelQuantity);
            } else if ("SELL".equals(order.getType())) {
                unlockSellRemainder(order, cancelQuantity);
            }
        }

        Order updatedOrder = Order.builder()
                .id(order.getId())
                .remainingQuantity(0L)
                .status(validateFinalStatus(event.finalStatus()))
                .build();

        orderMapper.updateRemainingQuantityAndStatus(updatedOrder);
    }

    private String validateFinalStatus(String status) {
        if (FILLED.equals(status) || PARTIALLY_FILLED.equals(status) || "CANCELED".equals(status)) {
            return status;
        }

        throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
    }

    private void unlockBuyRemainder(Order order, long cancelQuantity) {
        Wallet wallet = walletMapper.findByMemberIdForUpdate(order.getMemberId())
                .orElseThrow(() -> new BusinessException(ErrorCode.WALLET_NOT_FOUND));

        BigDecimal unlockAmount = order.getPrice()
                .multiply(BigDecimal.valueOf(cancelQuantity));

        BigDecimal updatedAvailableBalance =
                wallet.getAvailableBalance().add(unlockAmount);

        BigDecimal updatedLockedBalance =
                wallet.getLockedBalance().subtract(unlockAmount);

        Wallet updatedWallet = Wallet.builder()
                .id(wallet.getId())
                .memberId(wallet.getMemberId())
                .availableBalance(updatedAvailableBalance)
                .lockedBalance(updatedLockedBalance)
                .build();

        walletMapper.updateBalance(updatedWallet);

        WalletTransaction walletTransaction = WalletTransaction.builder()
                .walletId(wallet.getId())
                .type("ORDER_UNLOCK")
                .amount(unlockAmount)
                .availableBalanceAfter(updatedAvailableBalance)
                .lockedBalanceAfter(updatedLockedBalance)
                .referenceType("ORDER")
                .referenceId(order.getId())
                .build();

        walletTransactionMapper.insert(walletTransaction);
    }

    private void unlockSellRemainder(Order order, long cancelQuantity) {
        Holding holding = holdingMapper
                .findByMemberIdAndProductIdForUpdate(order.getMemberId(), order.getProductId())
                .orElseThrow(() -> new BusinessException(ErrorCode.INSUFFICIENT_HOLDING_QUANTITY));

        Holding updatedHolding = Holding.builder()
                .id(holding.getId())
                .memberId(holding.getMemberId())
                .productId(holding.getProductId())
                .quantity(holding.getQuantity())
                .lockedQuantity(holding.getLockedQuantity() - cancelQuantity)
                .averagePrice(holding.getAveragePrice())
                .build();

        holdingMapper.updateLockedQuantity(updatedHolding);
    }

    private void publishTradeSettledEvent(
            TradeExecutedEvent event,
            Trade trade,
            Order buyOrder,
            Order sellOrder,
            long executableQuantity
    ) {
        Product product = productMapper.findById(event.productId()).orElse(null);

        BigDecimal amount = event.price()
                .multiply(BigDecimal.valueOf(executableQuantity));

        Long occurredAt = event.occurredAt() == null
                ? System.currentTimeMillis()
                : event.occurredAt();

        ledgerEventPublisher.publishAfterCommit(new LedgerEvent(
                "TRADE_SETTLED-" + trade.getId(),
                "TRADE_SETTLED",
                "TRADE",
                trade.getId(),
                event.productId(),
                product == null ? null : product.getName(),
                product == null ? null : product.getCategory(),
                buyOrder.getId(),
                sellOrder.getId(),
                event.price(),
                executableQuantity,
                amount,
                occurredAt
        ));
    }

    private void saveCurrentPriceAfterCommit(Long productId, BigDecimal price) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                currentPriceCacheService.saveCurrentPrice(productId, price);
            }
        });
    }

    private OrderPair lockOrders(Long buyOrderId, Long sellOrderId) {
        if (buyOrderId < sellOrderId) {
            Order buyOrder = findOrderForUpdate(buyOrderId);
            Order sellOrder = findOrderForUpdate(sellOrderId);
            return new OrderPair(buyOrder, sellOrder);
        }

        Order sellOrder = findOrderForUpdate(sellOrderId);
        Order buyOrder = findOrderForUpdate(buyOrderId);
        return new OrderPair(buyOrder, sellOrder);
    }

    private Order findOrderForUpdate(Long orderId) {
        return orderMapper.findByIdForUpdate(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
    }

    private boolean isExecutableOrder(Order order) {
        return (OPEN.equals(order.getStatus()) || PARTIALLY_FILLED.equals(order.getStatus()))
                && order.getRemainingQuantity() != null
                && order.getRemainingQuantity() > 0;
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

    private void settleBuyer(Order buyOrder, BigDecimal tradePrice, long quantity, Trade trade) {
        Wallet buyerWallet = walletMapper.findByMemberIdForUpdate(buyOrder.getMemberId())
                .orElseThrow(() -> new BusinessException(ErrorCode.WALLET_NOT_FOUND));

        BigDecimal lockedAmount = buyOrder.getPrice()
                .multiply(BigDecimal.valueOf(quantity));

        BigDecimal actualTradeAmount = tradePrice
                .multiply(BigDecimal.valueOf(quantity));

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

        increaseBuyerHolding(buyOrder, tradePrice, quantity);
    }

    private void settleSeller(Order sellOrder, BigDecimal tradePrice, long quantity, Trade trade) {
        Wallet sellerWallet = walletMapper.findByMemberIdForUpdate(sellOrder.getMemberId())
                .orElseThrow(() -> new BusinessException(ErrorCode.WALLET_NOT_FOUND));

        BigDecimal tradeAmount = tradePrice
                .multiply(BigDecimal.valueOf(quantity));

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

        decreaseSellerHolding(sellOrder, quantity);
    }

    private void increaseBuyerHolding(Order buyOrder, BigDecimal tradePrice, long quantity) {
        Holding holding = holdingMapper
                .findByMemberIdAndProductIdForUpdate(buyOrder.getMemberId(), buyOrder.getProductId())
                .orElse(null);

        if (holding == null) {
            Holding newHolding = Holding.builder()
                    .memberId(buyOrder.getMemberId())
                    .productId(buyOrder.getProductId())
                    .quantity(quantity)
                    .lockedQuantity(0L)
                    .averagePrice(tradePrice)
                    .build();

            holdingMapper.insert(newHolding);
            return;
        }

        BigDecimal oldTotalAmount = holding.getAveragePrice()
                .multiply(BigDecimal.valueOf(holding.getQuantity()));

        BigDecimal newTradeAmount = tradePrice
                .multiply(BigDecimal.valueOf(quantity));

        long updatedQuantity = holding.getQuantity() + quantity;

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

    private void decreaseSellerHolding(Order sellOrder, long quantity) {
        Holding holding = holdingMapper
                .findByMemberIdAndProductIdForUpdate(sellOrder.getMemberId(), sellOrder.getProductId())
                .orElseThrow(() -> new BusinessException(ErrorCode.HOLDING_NOT_FOUND));

        Holding updatedHolding = Holding.builder()
                .id(holding.getId())
                .quantity(holding.getQuantity() - quantity)
                .lockedQuantity(holding.getLockedQuantity() - quantity)
                .averagePrice(holding.getAveragePrice())
                .build();

        holdingMapper.update(updatedHolding);
    }

    private record OrderPair(Order buyOrder, Order sellOrder) {
    }
}