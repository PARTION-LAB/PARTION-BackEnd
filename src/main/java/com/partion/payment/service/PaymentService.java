package com.partion.payment.service;

import com.partion.global.exception.BusinessException;
import com.partion.global.exception.ErrorCode;
import com.partion.global.response.PageResponse;
import com.partion.payment.dto.DepositHistoryResponse;
import com.partion.payment.mapper.DepositHistoryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class PaymentService {

    private final DepositHistoryMapper depositHistoryMapper;

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
}