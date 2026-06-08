package com.partion.product.mapper;

import com.partion.product.domain.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface ProductMapper {

    void insert(Product product);

    List<Product> findAll(
            @Param("category") String category,
            @Param("keyword") String keyword,
            @Param("limit") int limit,
            @Param("offset") int offset
    );

    long countAll(
            @Param("category") String category,
            @Param("keyword") String keyword
    );

    Optional<Product> findById(@Param("id") Long id);

    List<Product> findAllByIssuer(
            @Param("issuerMemberId") Long issuerMemberId,
            @Param("limit") int limit,
            @Param("offset") int offset
    );

    long countAllByIssuer(@Param("issuerMemberId") Long issuerMemberId);
}