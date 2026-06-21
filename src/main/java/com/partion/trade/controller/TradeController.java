package com.partion.trade.controller;

import com.partion.global.response.PageResponse;
import com.partion.global.security.CustomUserDetails;
import com.partion.trade.dto.MyTradeResponse;
import com.partion.trade.service.TradeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/trades")
public class TradeController {

    private final TradeService tradeService;

    @GetMapping("/me")
    public ResponseEntity<PageResponse<MyTradeResponse>> getMyTrades(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        PageResponse<MyTradeResponse> response =
                tradeService.getMyTrades(
                        userDetails.getMemberId(),
                        type,
                        page,
                        size
                );

        return ResponseEntity.ok(response);
    }
}