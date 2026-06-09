package com.partion.wallet.mapper;

import com.partion.wallet.domain.Wallet;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.Optional;

@Mapper
public interface WalletMapper {
    void insert(Wallet wallet);
    Optional<Wallet> findByMemberId(Long memberId);
    Optional<Wallet> findById(Long id);

    int increaseAvailableBalance(
            @Param("id") Long id,
            @Param("amount") BigDecimal amount
    );

    Optional<Wallet> findByMemberIdForUpdate(@Param("memberId") Long memberId);

    void updateBalance(Wallet wallet);
}
