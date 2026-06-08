package com.partion.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class CreateProductRequest {

    @NotBlank(message = "상품 종류는 필수입니다.")
    private String category;

    @NotBlank(message = "상품명은 필수입니다.")
    private String name;

    @NotBlank(message = "한 줄 요약은 필수입니다.")
    private String summary;

    @NotBlank(message = "상품 설명은 필수입니다.")
    private String description;

    @NotBlank(message = "상품 이미지는 필수입니다.")
    private String imageUrl;

    @NotBlank(message = "부가 정보는 필수입니다.")
    private String extraInfo;

    @NotNull(message = "목표 투자 금액은 필수입니다.")
    private BigDecimal targetAmount;

    @NotNull(message = "토큰 단가는 필수입니다.")
    private BigDecimal tokenPrice;

    private BigDecimal expectedYield;

    @NotNull(message = "모집 마감일은 필수입니다.")
    private LocalDate deadline;
}