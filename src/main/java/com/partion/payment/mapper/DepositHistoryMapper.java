package com.partion.payment.mapper;

import com.partion.payment.domain.DepositHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DepositHistoryMapper {

    List<DepositHistory> findByMemberId(
            @Param("memberId") Long memberId,
            @Param("limit") int limit,
            @Param("offset") int offset
    );

    long countByMemberId(@Param("memberId") Long memberId);
    void insert(DepositHistory depositHistory);
}