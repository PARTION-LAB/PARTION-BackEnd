package com.partion.payment.mapper;

import com.partion.payment.domain.DepositHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Mapper
public interface DepositHistoryMapper {

    List<DepositHistory> findByMemberId(
            @Param("memberId") Long memberId,
            @Param("limit") int limit,
            @Param("offset") int offset
    );

    long countByMemberId(@Param("memberId") Long memberId);
    void insert(DepositHistory depositHistory);

    Optional<DepositHistory> findByOrderId(@Param("orderId") String orderId);

    int updateDone(
            @Param("id") Long id,
            @Param("paymentKey") String paymentKey,
            @Param("approvedAt") LocalDateTime approvedAt
    );
}