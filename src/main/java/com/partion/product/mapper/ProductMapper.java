package com.partion.product.mapper;

import com.partion.product.domain.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
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

    List<Product> findAllByStatus(
            @Param("status") String status,
            @Param("category") String category,
            @Param("keyword") String keyword,
            @Param("limit") int limit,
            @Param("offset") int offset
    );

    long countAllByStatus(
            @Param("status") String status,
            @Param("category") String category,
            @Param("keyword") String keyword
    );

    Optional<Product> findByIdForUpdate(@Param("id") Long id);

    void updateFunding(Product product);

    int closeExpiredFundingProducts(@Param("today") LocalDate today);
}