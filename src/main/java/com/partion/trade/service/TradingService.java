package com.partion.trade.service;

import com.partion.global.exception.BusinessException;
import com.partion.global.exception.ErrorCode;
import com.partion.global.response.PageResponse;
import com.partion.order.mapper.OrderMapper;
import com.partion.product.dto.ProductListResponse;
import com.partion.product.mapper.ProductMapper;
import com.partion.trade.dto.OrderBookLevelResponse;
import com.partion.trade.dto.OrderBookResponse;
import com.partion.trade.dto.RecentTradeResponse;
import com.partion.trade.mapper.TradeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class TradingService {

    private static final String TRADING_STATUS = "TRADING";
    private static final String BUY = "BUY";
    private static final String SELL = "SELL";

    private final ProductMapper productMapper;
    private final TradeMapper tradeMapper;
    private final OrderMapper orderMapper;

    public PageResponse<ProductListResponse> getTradingProducts(
            String category,
            String keyword,
            int page,
            int size
    ) {
        validatePageRequest(page, size);

        int offset = page * size;

        List<ProductListResponse> content = productMapper
                .findAllByStatus(TRADING_STATUS, category, keyword, size, offset)
                .stream()
                .map(ProductListResponse::new)
                .toList();

        long totalElements = productMapper.countAllByStatus(TRADING_STATUS, category, keyword);

        return new PageResponse<>(content, page, size, totalElements);
    }

    private void validatePageRequest(int page, int size) {
        if (page < 0 || size <= 0 || size > 100) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    public List<RecentTradeResponse> getRecentTrades(Long productId, int size) {
        validateRecentTradeSize(size);
        validateTradingProduct(productId);

        return tradeMapper.findRecentTrades(productId, size);
    }

    private void validateRecentTradeSize(int size) {
        if (size <= 0 || size > 100) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    public OrderBookResponse getOrderBook(Long productId, int depth) {
        validateRecentTradeSize(depth);
        validateTradingProduct(productId);

        List<OrderBookLevelResponse> asks =
                orderMapper.findOrderBookLevels(productId, SELL, depth);
        List<OrderBookLevelResponse> bids =
                orderMapper.findOrderBookLevels(productId, BUY, depth);

        return new OrderBookResponse(productId, asks, bids);
    }

    private void validateTradingProduct(Long productId) {
        productMapper.findById(productId)
                .filter(product -> TRADING_STATUS.equals(product.getStatus()))
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_TRADING));
    }
}