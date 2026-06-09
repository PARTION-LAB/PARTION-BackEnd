package com.partion.comment.mapper;

import com.partion.comment.domain.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CommentMapper {

    void insert(Comment comment);

    List<Comment> findAllByBoardId(
            @Param("boardId") Long boardId,
            @Param("limit") int limit,
            @Param("offset") int offset
    );

    long countByBoardId(@Param("boardId") Long boardId);
}