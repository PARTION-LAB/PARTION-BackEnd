package com.partion.payment.service;

import com.partion.global.exception.BusinessException;
import com.partion.global.exception.ErrorCode;
import com.partion.global.response.PageResponse;
import com.partion.payment.client.TossPaymentClient;
import com.partion.payment.domain.DepositHistory;
import com.partion.payment.dto.*;
import com.partion.payment.mapper.DepositHistoryMapper;
import com.partion.wallet.domain.Wallet;
import com.partion.wallet.domain.WalletTransaction;
import com.partion.wallet.mapper.WalletMapper;
import com.partion.wallet.mapper.WalletTransactionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class PaymentService {

    private final DepositHistoryMapper depositHistoryMapper;
    private final WalletMapper walletMapper;
    private final TossPaymentClient tossPaymentClient;
    private final WalletTransactionMapper walletTransactionMapper;

    public PageResponse<DepositHistoryResponse> getMyDepositHistories(
            Long memberId,
            int page,
            int size
    ) {
        validatePageRequest(page, size);

        int offset = page * size;

        List<DepositHistoryResponse> content = depositHistoryMapper
                .findByMemberId(memberId, size, offset)
                .stream()
                .map(DepositHistoryResponse::new)
                .toList();

        long totalElements = depositHistoryMapper.countByMemberId(memberId);

        return new PageResponse<>(content, page, size, totalElements);
    }

    private void validatePageRequest(int page, int size) {
        if (page < 0 || size <= 0 || size > 100) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    @Transactional
    public DepositReadyResponse readyDeposit(Long memberId, DepositReadyRequest request) {
        validateDepositAmount(request.getAmount());

        Wallet wallet = walletMapper.findByMemberId(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.WALLET_NOT_FOUND));

        String orderId = generateOrderId(memberId);

        DepositHistory depositHistory = DepositHistory.builder()
                .memberId(memberId)
                .walletId(wallet.getId())
                .orderId(orderId)
                .amount(request.getAmount())
                .status("REQUESTED")
                .build();

        depositHistoryMapper.insert(depositHistory);

        return new DepositReadyResponse(
                depositHistory.getId(),
                depositHistory.getOrderId(),
                depositHistory.getAmount(),
                depositHistory.getStatus()
        );
    }

    private void validateDepositAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(ErrorCode.INVALID_DEPOSIT_AMOUNT);
        }
    }

    private String generateOrderId(Long memberId) {
        String random = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        return "deposit_" + LocalDate.now().toString().replace("-", "") + "_" + memberId + "_" + random;
    }

    @Transactional
    public DepositConfirmResponse confirmDeposit(Long memberId, DepositConfirmRequest request) {
        DepositHistory depositHistory = depositHistoryMapper.findByOrderId(request.getOrderId())
                .orElseThrow(() -> new BusinessException(ErrorCode.DEPOSIT_NOT_FOUND));

        if (!depositHistory.getMemberId().equals(memberId)) {
            throw new BusinessException(ErrorCode.DEPOSIT_NOT_FOUND);
        }

        if (!"REQUESTED".equals(depositHistory.getStatus())) {
            throw new BusinessException(ErrorCode.DEPOSIT_ALREADY_PROCESSED);
        }

        if (depositHistory.getAmount().compareTo(request.getAmount()) != 0) {
            throw new BusinessException(ErrorCode.DEPOSIT_AMOUNT_MISMATCH);
        }

        TossPaymentClient.TossConfirmResponse tossResponse =
                tossPaymentClient.confirm(
                        request.getPaymentKey(),
                        request.getOrderId(),
                        request.getAmount()
                );

        if (tossResponse == null || !"DONE".equals(tossResponse.getStatus())) {
            throw new BusinessException(ErrorCode.TOSS_PAYMENT_CONFIRM_FAILED);
        }

        if (!depositHistory.getOrderId().equals(tossResponse.getOrderId())) {
            throw new BusinessException(ErrorCode.TOSS_PAYMENT_CONFIRM_FAILED);
        }

        if (tossResponse.getTotalAmount().compareTo(depositHistory.getAmount()) != 0) {
            throw new BusinessException(ErrorCode.TOSS_PAYMENT_CONFIRM_FAILED);
        }

        LocalDateTime approvedAt = parseApprovedAt(tossResponse.getApprovedAt());

        int updatedRows = depositHistoryMapper.updateDone(
                depositHistory.getId(),
                request.getPaymentKey(),
                approvedAt
        );

        if (updatedRows == 0) {
            throw new BusinessException(ErrorCode.DEPOSIT_ALREADY_PROCESSED);
        }

        walletMapper.increaseAvailableBalance(
                depositHistory.getWalletId(),
                depositHistory.getAmount()
        );

        Wallet updatedWallet = walletMapper.findById(depositHistory.getWalletId())
                .orElseThrow(() -> new BusinessException(ErrorCode.WALLET_NOT_FOUND));

        WalletTransaction walletTransaction = WalletTransaction.builder()
                .walletId(updatedWallet.getId())
                .type("DEPOSIT")
                .amount(depositHistory.getAmount())
                .availableBalanceAfter(updatedWallet.getAvailableBalance())
                .lockedBalanceAfter(updatedWallet.getLockedBalance())
                .referenceType("DEPOSIT_HISTORY")
                .referenceId(depositHistory.getId())
                .build();

        walletTransactionMapper.insert(walletTransaction);

        return new DepositConfirmResponse(
                depositHistory.getId(),
                depositHistory.getOrderId(),
                request.getPaymentKey(),
                depositHistory.getAmount(),
                "DONE",
                approvedAt
        );
    }

    private LocalDateTime parseApprovedAt(String approvedAt) {
        if (approvedAt == null) {
            return LocalDateTime.now();
        }

        return OffsetDateTime.parse(approvedAt).toLocalDateTime();
    }
}