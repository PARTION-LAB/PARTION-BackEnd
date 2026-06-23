package com.partion.matching.service;

import com.partion.matching.dto.OrderBookResyncResponse;
import com.partion.matching.producer.OrderCommandPublisher;
import com.partion.order.domain.Order;
import com.partion.order.mapper.OrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class OrderBookResyncService {

    private final OrderMapper orderMapper;
    private final OrderCommandPublisher orderCommandPublisher;

    public OrderBookResyncResponse resyncActiveOrders() {
        List<Order> activeOrders = orderMapper.findActiveOrdersForResync();

        for (Order order : activeOrders) {
            orderCommandPublisher.publishResync(order);
        }

        return new OrderBookResyncResponse(activeOrders.size());
    }
}