package com.partion.comment.dto;

import com.partion.comment.domain.Comment;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class CommentCreateResponse {

    private final Long commentId;
    private final Long boardId;
    private final Long memberId;
    private final String content;
    private final LocalDateTime createdAt;

    public CommentCreateResponse(Comment comment) {
        this.commentId = comment.getId();
        this.boardId = comment.getBoardId();
        this.memberId = comment.getMemberId();
        this.content = comment.getContent();
        this.createdAt = comment.getCreatedAt();
    }
}