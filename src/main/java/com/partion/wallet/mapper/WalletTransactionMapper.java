package com.partion.wallet.mapper;

import com.partion.wallet.domain.WalletTransaction;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface WalletTransactionMapper {

    List<WalletTransaction> findByWalletId(
            @Param("walletId") Long walletId,
            @Param("limit") int limit,
            @Param("offset") int offset
    );

    long countByWalletId(@Param("walletId") Long walletId);
}