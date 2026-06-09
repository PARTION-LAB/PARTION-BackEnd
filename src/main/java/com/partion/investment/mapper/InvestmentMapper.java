package com.partion.investment.mapper;

import com.partion.investment.domain.Investment;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface InvestmentMapper {

    void insert(Investment investment);
}