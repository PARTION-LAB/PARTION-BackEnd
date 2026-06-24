package com.partion.board.dto;

import com.partion.board.domain.Board;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class BoardListResponse {

    private final Long boardId;
    private final Long memberId;
    private final String writerNickname;
    private final String category;
    private final String title;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public BoardListResponse(Board board) {
        this.boardId = board.getId();
        this.memberId = board.getMemberId();
        this.writerNickname = board.getWriterNickname();
        this.category = board.getCategory();
        this.title = board.getTitle();
        this.createdAt = board.getCreatedAt();
        this.updatedAt = board.getUpdatedAt();
    }
}
