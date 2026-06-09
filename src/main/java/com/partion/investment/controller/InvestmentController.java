package com.partion.investment.controller;

import com.partion.global.response.PageResponse;
import com.partion.global.security.CustomUserDetails;
import com.partion.investment.dto.CreateInvestmentRequest;
import com.partion.investment.dto.InvestmentCreateResponse;
import com.partion.investment.service.InvestmentService;
import com.partion.product.dto.ProductDetailResponse;
import com.partion.product.dto.ProductListResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/investments")
public class InvestmentController {

    private final InvestmentService investmentService;

    @GetMapping("/products")
    public ResponseEntity<PageResponse<ProductListResponse>> getFundingProducts(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        PageResponse<ProductListResponse> response =
                investmentService.getFundingProducts(category, keyword, page, size);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/products/{productId}")
    public ResponseEntity<ProductDetailResponse> getFundingProductDetail(
            @PathVariable Long productId
    ) {
        ProductDetailResponse response =
                investmentService.getFundingProductDetail(productId);

        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<InvestmentCreateResponse> invest(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CreateInvestmentRequest request
    ) {
        InvestmentCreateResponse response =
                investmentService.invest(userDetails.getMemberId(), request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}