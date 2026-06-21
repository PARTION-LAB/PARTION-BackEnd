package com.partion.portfolio.mapper;

import com.partion.portfolio.domain.Holding;
import com.partion.portfolio.dto.HoldingResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Mapper
public interface HoldingMapper {

    Optional<Holding> findByMemberIdAndProductIdForUpdate(
            @Param("memberId") Long memberId,
            @Param("productId") Long productId
    );

    void insert(Holding holding);

    void update(Holding holding);

    List<HoldingResponse> findMyHoldings(
            @Param("memberId") Long memberId,
            @Param("limit") int limit,
            @Param("offset") int offset
    );

    long countMyHoldings(@Param("memberId") Long memberId);

    BigDecimal sumTokenValuationAmount(@Param("memberId") Long memberId);

    BigDecimal sumExpectedAnnualDividend(@Param("memberId") Long memberId);

    void updateLockedQuantity(Holding holding);

    List<HoldingResponse> findAllMyHoldings(@Param("memberId") Long memberId);
}