package com.partion.product.service;

import com.partion.global.exception.BusinessException;
import com.partion.global.exception.ErrorCode;
import com.partion.global.response.PageResponse;
import com.partion.product.domain.Product;
import com.partion.product.dto.CreateProductRequest;
import com.partion.product.dto.ProductCreateResponse;
import com.partion.product.dto.ProductListResponse;
import com.partion.product.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class ProductService {

    private final ProductMapper productMapper;

    @Transactional
    public ProductCreateResponse createProduct(Long memberId, CreateProductRequest request) {
        validateProductAmount(request.getTargetAmount(), request.getTokenPrice());
        validateDeadline(request.getDeadline());

        Long totalTokenQuantity =
                calculateTotalTokenQuantity(request.getTargetAmount(), request.getTokenPrice());

        Product product = Product.builder()
                .issuerMemberId(memberId)
                .category(request.getCategory())
                .name(request.getName())
                .summary(request.getSummary())
                .description(request.getDescription())
                .imageUrl(request.getImageUrl())
                .extraInfo(request.getExtraInfo())
                .targetAmount(request.getTargetAmount())
                .currentAmount(BigDecimal.ZERO)
                .tokenPrice(request.getTokenPrice())
                .totalTokenQuantity(totalTokenQuantity)
                .fundedTokenQuantity(0L)
                .expectedYield(request.getExpectedYield())
                .deadline(request.getDeadline())
                .status("FUNDING")
                .build();

        productMapper.insert(product);

        return new ProductCreateResponse(product);
    }

    private void validateProductAmount(BigDecimal targetAmount, BigDecimal tokenPrice) {
        if (targetAmount == null || tokenPrice == null
                || targetAmount.compareTo(BigDecimal.ZERO) <= 0
                || tokenPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(ErrorCode.INVALID_PRODUCT_AMOUNT);
        }
    }

    private void validateDeadline(LocalDate deadline) {
        if (deadline == null || !deadline.isAfter(LocalDate.now())) {
            throw new BusinessException(ErrorCode.INVALID_PRODUCT_DEADLINE);
        }
    }

    private Long calculateTotalTokenQuantity(BigDecimal targetAmount, BigDecimal tokenPrice) {
        BigDecimal[] divideAndRemainder = targetAmount.divideAndRemainder(tokenPrice);

        if (divideAndRemainder[1].compareTo(BigDecimal.ZERO) != 0) {
            throw new BusinessException(ErrorCode.INVALID_PRODUCT_TOKEN_QUANTITY);
        }

        return divideAndRemainder[0].longValueExact();
    }

    public PageResponse<ProductListResponse> getProducts(
            String category,
            String keyword,
            int page,
            int size
    ) {
        validatePageRequest(page, size);

        int offset = page * size;

        List<ProductListResponse> content = productMapper
                .findAll(category, keyword, size, offset)
                .stream()
                .map(ProductListResponse::new)
                .toList();

        long totalElements = productMapper.countAll(category, keyword);

        return new PageResponse<>(content, page, size, totalElements);
    }

    private void validatePageRequest(int page, int size) {
        if (page < 0 || size <= 0 || size > 100) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }
}