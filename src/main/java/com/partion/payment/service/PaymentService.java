package com.partion.payment.service;

import com.partion.global.exception.BusinessException;
import com.partion.global.exception.ErrorCode;
import com.partion.global.response.PageResponse;
import com.partion.payment.domain.DepositHistory;
import com.partion.payment.dto.DepositHistoryResponse;
import com.partion.payment.dto.DepositReadyRequest;
import com.partion.payment.dto.DepositReadyResponse;
import com.partion.payment.mapper.DepositHistoryMapper;
import com.partion.wallet.domain.Wallet;
import com.partion.wallet.mapper.WalletMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class PaymentService {

    private final DepositHistoryMapper depositHistoryMapper;
    private final WalletMapper walletMapper;

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
}