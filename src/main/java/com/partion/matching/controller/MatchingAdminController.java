package com.partion.matching.controller;

import com.partion.matching.dto.OrderBookResyncResponse;
import com.partion.matching.service.OrderBookResyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class MatchingAdminController {

    private final OrderBookResyncService orderBookResyncService;

    @PostMapping("/api/matching/orders/resync")
    public OrderBookResyncResponse resyncActiveOrders() {
        return orderBookResyncService.resyncActiveOrders();
    }
}
