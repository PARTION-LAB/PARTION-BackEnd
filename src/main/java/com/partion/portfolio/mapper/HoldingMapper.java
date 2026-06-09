package com.partion.portfolio.mapper;

import com.partion.portfolio.domain.Holding;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Optional;

@Mapper
public interface HoldingMapper {

    Optional<Holding> findByMemberIdAndProductIdForUpdate(
            @Param("memberId") Long memberId,
            @Param("productId") Long productId
    );

    void insert(Holding holding);

    void update(Holding holding);
}