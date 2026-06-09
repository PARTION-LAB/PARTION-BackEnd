package com.partion.investment.controller;

import com.partion.global.response.PageResponse;
import com.partion.investment.service.InvestmentService;
import com.partion.product.dto.ProductListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
}