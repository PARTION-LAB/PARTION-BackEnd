package com.partion.trade.controller;

import com.partion.global.response.PageResponse;
import com.partion.product.dto.ProductListResponse;
import com.partion.trade.service.TradingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/trading")
public class TradingController {

    private final TradingService tradingService;

    @GetMapping("/products")
    public ResponseEntity<PageResponse<ProductListResponse>> getTradingProducts(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        PageResponse<ProductListResponse> response =
                tradingService.getTradingProducts(category, keyword, page, size);

        return ResponseEntity.ok(response);
    }
}