package com.partion.ledger.mapper;

import com.partion.ledger.dto.LedgerBlockRow;
import com.partion.ledger.dto.LedgerTransactionRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface LedgerMapper {

    List<LedgerBlockRow> findBlocks(
            @Param("limit") int limit,
            @Param("offset") int offset
    );

    long countBlocks();

    Optional<LedgerBlockRow> findBlockByNumber(@Param("blockNumber") Long blockNumber);

    Optional<LedgerBlockRow> findLatestBlock();

    List<LedgerBlockRow> findAllBlocksForVerify();

    List<LedgerTransactionRow> findTransactionsByBlockId(@Param("blockId") Long blockId);

    List<LedgerTransactionRow> findTransactions(
            @Param("limit") int limit,
            @Param("offset") int offset
    );

    long countTransactions();

    Optional<LedgerTransactionRow> findTransactionByHash(@Param("transactionHash") String transactionHash);
}