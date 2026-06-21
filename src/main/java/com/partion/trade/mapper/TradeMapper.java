package com.partion.trade.mapper;

import com.partion.trade.domain.Trade;
import com.partion.trade.dto.MyTradeResponse;
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

    List<MyTradeResponse> findMyTrades(
            @Param("memberId") Long memberId,
            @Param("type") String type,
            @Param("limit") int limit,
            @Param("offset") int offset
    );

    long countMyTrades(
            @Param("memberId") Long memberId,
            @Param("type") String type
    );
}