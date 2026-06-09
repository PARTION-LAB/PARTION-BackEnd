package com.partion.board.service;

import com.partion.board.domain.Board;
import com.partion.board.dto.*;
import com.partion.board.mapper.BoardMapper;
import com.partion.global.exception.BusinessException;
import com.partion.global.exception.ErrorCode;
import com.partion.global.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

    @Transactional(readOnly = true)
    public PageResponse<BoardListResponse> getBoards(String category, int page, int size) {
        validatePageRequest(page, size);

        int offset = page * size;

        List<BoardListResponse> content = boardMapper.findAll(category, size, offset)
                .stream()
                .map(BoardListResponse::new)
                .toList();

        long totalElements = boardMapper.countAll(category);

        return new PageResponse<>(content, page, size, totalElements);
    }

    private void validatePageRequest(int page, int size) {
        if (page < 0 || size <= 0 || size > 100) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    @Transactional(readOnly = true)
    public BoardDetailResponse getBoardDetail(Long boardId) {
        Board board = boardMapper.findById(boardId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BOARD_NOT_FOUND));

        return new BoardDetailResponse(board);
    }

    @Transactional
    public BoardUpdateResponse updateBoard(Long memberId, Long boardId, UpdateBoardRequest request) {
        Board board = boardMapper.findById(boardId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BOARD_NOT_FOUND));

        if (!board.getMemberId().equals(memberId)) {
            throw new BusinessException(ErrorCode.BOARD_ACCESS_DENIED);
        }

        Board updateBoard = Board.builder()
                .id(boardId)
                .memberId(memberId)
                .category(request.getCategory())
                .title(request.getTitle())
                .content(request.getContent())
                .build();

        boardMapper.update(updateBoard);

        Board updatedBoard = boardMapper.findById(boardId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BOARD_NOT_FOUND));

        return new BoardUpdateResponse(updatedBoard);
    }
}