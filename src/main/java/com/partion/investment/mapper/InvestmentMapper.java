package com.partion.investment.mapper;

import com.partion.investment.domain.Investment;
import com.partion.investment.dto.MyInvestmentResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface InvestmentMapper {

    void insert(Investment investment);

    List<MyInvestmentResponse> findMyInvestments(
            @Param("memberId") Long memberId,
            @Param("limit") int limit,
            @Param("offset") int offset
    );

    long countMyInvestments(@Param("memberId") Long memberId);
}