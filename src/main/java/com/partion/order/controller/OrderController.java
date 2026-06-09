package com.partion.order.controller;

import com.partion.global.security.CustomUserDetails;
import com.partion.order.dto.CreateOrderRequest;
import com.partion.order.dto.OrderCreateResponse;
import com.partion.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderCreateResponse> createOrder(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CreateOrderRequest request
    ) {
        OrderCreateResponse response =
                orderService.createOrder(userDetails.getMemberId(), request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}