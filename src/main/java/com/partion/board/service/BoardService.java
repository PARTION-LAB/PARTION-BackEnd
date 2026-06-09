package com.partion.board.service;

import com.partion.board.domain.Board;
import com.partion.board.dto.BoardCreateResponse;
import com.partion.board.dto.CreateBoardRequest;
import com.partion.board.mapper.BoardMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class BoardService {

    private final BoardMapper boardMapper;

    @Transactional
    public BoardCreateResponse createBoard(Long memberId, CreateBoardRequest request) {
        Board board = Board.builder()
                .memberId(memberId)
                .category(request.getCategory())
                .title(request.getTitle())
                .content(request.getContent())
                .build();

        boardMapper.insert(board);

        return new BoardCreateResponse(board);
    }
}