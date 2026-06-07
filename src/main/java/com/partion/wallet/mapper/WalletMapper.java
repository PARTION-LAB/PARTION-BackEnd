package com.partion.wallet.mapper;

import com.partion.wallet.domain.Wallet;
import org.apache.ibatis.annotations.Mapper;

import java.util.Optional;

@Mapper
public interface WalletMapper {
    void insert(Wallet wallet);
    Optional<Wallet> findByMemberId(Long memberId);
}
