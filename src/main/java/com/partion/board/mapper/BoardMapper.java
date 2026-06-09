package com.partion.board.mapper;

import com.partion.board.domain.Board;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BoardMapper {

    void insert(Board board);
}