package com.partion.ledger.service;

import com.partion.global.response.PageResponse;
import com.partion.ledger.dto.LedgerBlockResponse;
import com.partion.ledger.dto.LedgerBlockRow;
import com.partion.ledger.dto.LedgerTransactionResponse;
import com.partion.ledger.dto.LedgerTransactionRow;
import com.partion.ledger.dto.LedgerVerifyResponse;
import com.partion.ledger.mapper.LedgerMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class LedgerService {

    private static final String GENESIS_PREVIOUS_HASH =
            "0x0000000000000000000000000000000000000000000000000000000000000000";

    private final LedgerMapper ledgerMapper;
    private final ObjectMapper objectMapper;

    public PageResponse<LedgerBlockResponse> getBlocks(int page, int size) {
        int safePage = Math.max(0, page);
        int safeSize = Math.min(Math.max(1, size), 50);
        int offset = safePage * safeSize;

        List<LedgerBlockResponse> content = ledgerMapper.findBlocks(safeSize, offset).stream()
                .map(this::toBlockResponse)
                .toList();

        return new PageResponse<>(content, safePage, safeSize, ledgerMapper.countBlocks());
    }

    public LedgerBlockResponse getBlock(Long blockNumber) {
        LedgerBlockRow row = ledgerMapper.findBlockByNumber(blockNumber)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ledger block not found."));

        return toBlockResponse(row);
    }

    public PageResponse<LedgerTransactionResponse> getTransactions(int page, int size) {
        int safePage = Math.max(0, page);
        int safeSize = Math.min(Math.max(1, size), 100);
        int offset = safePage * safeSize;

        List<LedgerTransactionResponse> content = ledgerMapper.findTransactions(safeSize, offset).stream()
                .map(this::toTransactionResponse)
                .toList();

        return new PageResponse<>(content, safePage, safeSize, ledgerMapper.countTransactions());
    }

    public LedgerTransactionResponse getTransaction(String transactionHash) {
        LedgerTransactionRow row = ledgerMapper.findTransactionByHash(transactionHash)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ledger transaction not found."));

        return toTransactionResponse(row);
    }

    public LedgerVerifyResponse verify() {
        List<LedgerBlockRow> blocks = ledgerMapper.findAllBlocksForVerify();
        if (blocks.isEmpty()) {
            return new LedgerVerifyResponse(true, 0, 0, "", "Ledger is empty.");
        }

        String expectedPreviousHash = GENESIS_PREVIOUS_HASH;
        long expectedBlockNumber = 0;

        for (LedgerBlockRow block : blocks) {
            if (block.getBlockNumber() == null || block.getBlockNumber() != expectedBlockNumber) {
                return invalid(blocks, "Block number sequence is broken.");
            }

            if (!expectedPreviousHash.equals(block.getPreviousHash())) {
                return invalid(blocks, "Previous hash chain is broken.");
            }

            if (block.getCurrentHash() == null || block.getCurrentHash().isBlank()) {
                return invalid(blocks, "Current hash is missing.");
            }

            expectedPreviousHash = block.getCurrentHash();
            expectedBlockNumber += 1;
        }

        LedgerBlockRow latest = blocks.get(blocks.size() - 1);
        return new LedgerVerifyResponse(
                true,
                latest.getBlockNumber(),
                blocks.size(),
                latest.getCurrentHash(),
                "Ledger hash chain is valid."
        );
    }

    private LedgerVerifyResponse invalid(List<LedgerBlockRow> blocks, String message) {
        LedgerBlockRow latest = blocks.get(blocks.size() - 1);
        return new LedgerVerifyResponse(
                false,
                latest.getBlockNumber(),
                blocks.size(),
                latest.getCurrentHash(),
                message
        );
    }

    private LedgerBlockResponse toBlockResponse(LedgerBlockRow row) {
        List<LedgerTransactionResponse> transactions = ledgerMapper.findTransactionsByBlockId(row.getId()).stream()
                .map(this::toTransactionResponse)
                .toList();

        return new LedgerBlockResponse(row, transactions);
    }

    private LedgerTransactionResponse toTransactionResponse(LedgerTransactionRow row) {
        return new LedgerTransactionResponse(row, parsePayload(row.getPayloadJson()));
    }

    private Object parsePayload(String payloadJson) {
        if (payloadJson == null || payloadJson.isBlank()) {
            return Map.of();
        }

        try {
            return objectMapper.readValue(payloadJson, new TypeReference<Map<String, Object>>() {});
        } catch (JacksonException exception) {
            return payloadJson;
        }
    }
}