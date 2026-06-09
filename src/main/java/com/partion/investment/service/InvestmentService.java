package com.partion.investment.service;

import com.partion.global.exception.BusinessException;
import com.partion.global.exception.ErrorCode;
import com.partion.global.response.PageResponse;
import com.partion.product.dto.ProductListResponse;
import com.partion.product.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class InvestmentService {

    private static final String FUNDING_STATUS = "FUNDING";

    private final ProductMapper productMapper;

    public PageResponse<ProductListResponse> getFundingProducts(
            String category,
            String keyword,
            int page,
            int size
    ) {
        validatePageRequest(page, size);

        int offset = page * size;

        List<ProductListResponse> content = productMapper
                .findAllByStatus(FUNDING_STATUS, category, keyword, size, offset)
                .stream()
                .map(ProductListResponse::new)
                .toList();

        long totalElements = productMapper.countAllByStatus(FUNDING_STATUS, category, keyword);

        return new PageResponse<>(content, page, size, totalElements);
    }

    private void validatePageRequest(int page, int size) {
        if (page < 0 || size <= 0 || size > 100) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }
}