package com.partion.wallet.mapper;

import com.partion.wallet.domain.Wallet;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface WalletMapper {
    void insert(Wallet wallet);
}
