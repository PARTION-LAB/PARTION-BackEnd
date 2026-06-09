package com.partion.order.mapper;

import com.partion.order.domain.Order;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderMapper {

    void insert(Order order);
}