package com.partion.product.mapper;

import com.partion.product.domain.Product;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ProductMapper {

    void insert(Product product);
}