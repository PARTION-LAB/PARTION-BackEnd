package com.partion.order.mapper;

import com.partion.order.domain.Order;
import com.partion.order.dto.MyOrderResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface OrderMapper {

    void insert(Order order);

    List<MyOrderResponse> findMyOrders(
            @Param("memberId") Long memberId,
            @Param("type") String type,
            @Param("status") String status,
            @Param("limit") int limit,
            @Param("offset") int offset
    );

    long countMyOrders(
            @Param("memberId") Long memberId,
            @Param("type") String type,
            @Param("status") String status
    );

    Optional<Order> findByIdForUpdate(@Param("id") Long id);

    void updateStatus(Order order);

    void updateRemainingQuantityAndStatus(Order order);
}