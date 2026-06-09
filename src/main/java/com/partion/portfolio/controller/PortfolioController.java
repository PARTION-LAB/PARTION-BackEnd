package com.partion.portfolio.controller;

import com.partion.global.response.PageResponse;
import com.partion.global.security.CustomUserDetails;
import com.partion.portfolio.dto.HoldingResponse;
import com.partion.portfolio.dto.PortfolioSummaryResponse;
import com.partion.portfolio.service.PortfolioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/portfolio")
public class PortfolioController {

    private final PortfolioService portfolioService;

    @GetMapping("/holdings")
    public ResponseEntity<PageResponse<HoldingResponse>> getMyHoldings(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        PageResponse<HoldingResponse> response =
                portfolioService.getMyHoldings(userDetails.getMemberId(), page, size);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/summary")
    public ResponseEntity<PortfolioSummaryResponse> getSummary(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        PortfolioSummaryResponse response =
                portfolioService.getSummary(userDetails.getMemberId());

        return ResponseEntity.ok(response);
    }
}