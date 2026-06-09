package com.partion.board.mapper;

import com.partion.board.domain.Board;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BoardMapper {

    void insert(Board board);

    List<Board> findAll(
            @Param("category") String category,
            @Param("limit") int limit,
            @Param("offset") int offset
    );

    long countAll(@Param("category") String category);
}