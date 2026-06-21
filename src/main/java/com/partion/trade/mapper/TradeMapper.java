package com.partion.trade.mapper;

import com.partion.trade.domain.Trade;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TradeMapper {

    void insert(Trade trade);
}