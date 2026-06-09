package com.partion.board.dto;

import com.partion.board.domain.Board;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class BoardUpdateResponse {

    private final Long boardId;
    private final Long memberId;
    private final String category;
    private final String title;
    private final String content;
    private final LocalDateTime updatedAt;

    public BoardUpdateResponse(Board board) {
        this.boardId = board.getId();
        this.memberId = board.getMemberId();
        this.category = board.getCategory();
        this.title = board.getTitle();
        this.content = board.getContent();
        this.updatedAt = board.getUpdatedAt();
    }
}