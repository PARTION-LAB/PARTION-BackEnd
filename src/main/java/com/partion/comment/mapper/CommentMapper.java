package com.partion.comment.mapper;

import com.partion.comment.domain.Comment;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CommentMapper {

    void insert(Comment comment);
}