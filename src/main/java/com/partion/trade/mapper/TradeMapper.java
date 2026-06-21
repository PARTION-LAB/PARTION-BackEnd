package com.partion.trade.mapper;

import com.partion.trade.domain.Trade;
import com.partion.trade.dto.RecentTradeResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TradeMapper {

    void insert(Trade trade);

    List<RecentTradeResponse> findRecentTrades(
            @Param("productId") Long productId,
            @Param("limit") int limit
    );
}