package com.partion.order.service;

import com.partion.global.exception.BusinessException;
import com.partion.global.exception.ErrorCode;
import com.partion.global.response.PageResponse;
import com.partion.order.domain.Order;
import com.partion.order.dto.CreateOrderRequest;
import com.partion.order.dto.MyOrderResponse;
import com.partion.order.dto.OrderCreateResponse;
import com.partion.order.mapper.OrderMapper;
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
import java.util.List;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class OrderService {

    private static final String TRADING_STATUS = "TRADING";
    private static final String BUY = "BUY";
    private static final String SELL = "SELL";
    private static final String LIMIT = "LIMIT";
    private static final String OPEN = "OPEN";
    private static final String PARTIALLY_FILLED = "PARTIALLY_FILLED";
    private static final String CANCELED = "CANCELED";

    private final OrderMapper orderMapper;
    private final ProductMapper productMapper;
    private final WalletMapper walletMapper;
    private final WalletTransactionMapper walletTransactionMapper;
    private final HoldingMapper holdingMapper;

    @Transactional
    public OrderCreateResponse createOrder(Long memberId, CreateOrderRequest request) {
        validateOrderRequest(request);

        Product product = productMapper.findById(request.getProductId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        if (!TRADING_STATUS.equals(product.getStatus())) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_TRADING);
        }

        Order order = Order.builder()
                .memberId(memberId)
                .productId(product.getId())
                .type(request.getType())
                .orderMethod(request.getOrderMethod())
                .price(request.getPrice())
                .quantity(request.getQuantity())
                .remainingQuantity(request.getQuantity())
                .status(OPEN)
                .build();

        if (BUY.equals(request.getType())) {
            lockBuyAmount(memberId, order);
        } else if (SELL.equals(request.getType())) {
            lockSellQuantity(memberId, order);
        } else {
            throw new BusinessException(ErrorCode.INVALID_ORDER_TYPE);
        }

        orderMapper.insert(order);

        return new OrderCreateResponse(order);
    }

    private void validateOrderRequest(CreateOrderRequest request) {
        if (!BUY.equals(request.getType()) && !SELL.equals(request.getType())) {
            throw new BusinessException(ErrorCode.INVALID_ORDER_TYPE);
        }

        if (!LIMIT.equals(request.getOrderMethod())) {
            throw new BusinessException(ErrorCode.INVALID_ORDER_METHOD);
        }

        if (request.getPrice() == null || request.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(ErrorCode.INVALID_ORDER_PRICE);
        }

        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new BusinessException(ErrorCode.INVALID_ORDER_QUANTITY);
        }
    }

    private void lockBuyAmount(Long memberId, Order order) {
        Wallet wallet = walletMapper.findByMemberIdForUpdate(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.WALLET_NOT_FOUND));

        BigDecimal orderAmount = order.getPrice()
                .multiply(BigDecimal.valueOf(order.getQuantity()));

        if (wallet.getAvailableBalance().compareTo(orderAmount) < 0) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_BALANCE);
        }

        BigDecimal updatedAvailableBalance =
                wallet.getAvailableBalance().subtract(orderAmount);

        BigDecimal updatedLockedBalance =
                wallet.getLockedBalance().add(orderAmount);

        Wallet updatedWallet = Wallet.builder()
                .id(wallet.getId())
                .memberId(wallet.getMemberId())
                .availableBalance(updatedAvailableBalance)
                .lockedBalance(updatedLockedBalance)
                .build();

        walletMapper.updateBalance(updatedWallet);

        WalletTransaction walletTransaction = WalletTransaction.builder()
                .walletId(wallet.getId())
                .type("ORDER_LOCK")
                .amount(orderAmount.negate())
                .availableBalanceAfter(updatedAvailableBalance)
                .lockedBalanceAfter(updatedLockedBalance)
                .referenceType("ORDER")
                .referenceId(null)
                .build();

        walletTransactionMapper.insert(walletTransaction);
    }

    private void lockSellQuantity(Long memberId, Order order) {
        Holding holding = holdingMapper
                .findByMemberIdAndProductIdForUpdate(memberId, order.getProductId())
                .orElseThrow(() -> new BusinessException(ErrorCode.INSUFFICIENT_HOLDING_QUANTITY));

        long availableQuantity = holding.getQuantity() - holding.getLockedQuantity();

        if (availableQuantity < order.getQuantity()) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_HOLDING_QUANTITY);
        }

        Holding updatedHolding = Holding.builder()
                .id(holding.getId())
                .memberId(holding.getMemberId())
                .productId(holding.getProductId())
                .quantity(holding.getQuantity())
                .lockedQuantity(holding.getLockedQuantity() + order.getQuantity())
                .averagePrice(holding.getAveragePrice())
                .build();

        holdingMapper.updateLockedQuantity(updatedHolding);
    }

    public PageResponse<MyOrderResponse> getMyOrders(
            Long memberId,
            String type,
            String status,
            int page,
            int size
    ) {
        validatePageRequest(page, size);

        int offset = page * size;

        List<MyOrderResponse> content =
                orderMapper.findMyOrders(memberId, type, status, size, offset);

        long totalElements =
                orderMapper.countMyOrders(memberId, type, status);

        return new PageResponse<>(content, page, size, totalElements);
    }

    private void validatePageRequest(int page, int size) {
        if (page < 0 || size <= 0 || size > 100) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    @Transactional
    public void cancelOrder(Long memberId, Long orderId) {
        Order order = orderMapper.findByIdForUpdate(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        if (!order.getMemberId().equals(memberId)) {
            throw new BusinessException(ErrorCode.ORDER_ACCESS_DENIED);
        }

        if (!OPEN.equals(order.getStatus()) && !PARTIALLY_FILLED.equals(order.getStatus())) {
            throw new BusinessException(ErrorCode.ORDER_NOT_CANCELABLE);
        }

        if (BUY.equals(order.getType())) {
            unlockBuyAmount(order);
        } else if (SELL.equals(order.getType())) {
            unlockSellQuantity(order);
        }

        Order canceledOrder = Order.builder()
                .id(order.getId())
                .status(CANCELED)
                .build();

        orderMapper.updateStatus(canceledOrder);
    }

    private void unlockBuyAmount(Order order) {
        Wallet wallet = walletMapper.findByMemberIdForUpdate(order.getMemberId())
                .orElseThrow(() -> new BusinessException(ErrorCode.WALLET_NOT_FOUND));

        BigDecimal unlockAmount = order.getPrice()
                .multiply(BigDecimal.valueOf(order.getRemainingQuantity()));

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

    private void unlockSellQuantity(Order order) {
        Holding holding = holdingMapper
                .findByMemberIdAndProductIdForUpdate(order.getMemberId(), order.getProductId())
                .orElseThrow(() -> new BusinessException(ErrorCode.INSUFFICIENT_HOLDING_QUANTITY));

        Holding updatedHolding = Holding.builder()
                .id(holding.getId())
                .memberId(holding.getMemberId())
                .productId(holding.getProductId())
                .quantity(holding.getQuantity())
                .lockedQuantity(holding.getLockedQuantity() - order.getRemainingQuantity())
                .averagePrice(holding.getAveragePrice())
                .build();

        holdingMapper.updateLockedQuantity(updatedHolding);
    }
}