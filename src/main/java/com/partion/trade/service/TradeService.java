package com.partion.trade.service;

import com.partion.global.exception.BusinessException;
import com.partion.global.exception.ErrorCode;
import com.partion.global.response.PageResponse;
import com.partion.trade.dto.MyTradeResponse;
import com.partion.trade.mapper.TradeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class TradeService {

    private final TradeMapper tradeMapper;

    public PageResponse<MyTradeResponse> getMyTrades(
            Long memberId,
            String type,
            int page,
            int size
    ) {
        validatePageRequest(page, size);
        validateType(type);

        int offset = page * size;

        List<MyTradeResponse> content =
                tradeMapper.findMyTrades(memberId, type, size, offset);

        long totalElements =
                tradeMapper.countMyTrades(memberId, type);

        return new PageResponse<>(content, page, size, totalElements);
    }

    private void validatePageRequest(int page, int size) {
        if (page < 0 || size <= 0 || size > 100) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    private void validateType(String type) {
        if (type == null || type.isBlank()) {
            return;
        }

        if (!"BUY".equals(type) && !"SELL".equals(type)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }
}