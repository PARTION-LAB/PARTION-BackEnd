package com.partion.comment.domain;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class Comment {

    private Long id;
    private Long boardId;
    private Long memberId;
    private String writerNickname;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
