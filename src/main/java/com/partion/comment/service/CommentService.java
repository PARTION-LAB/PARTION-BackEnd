package com.partion.comment.service;

import com.partion.board.mapper.BoardMapper;
import com.partion.comment.domain.Comment;
import com.partion.comment.dto.CommentCreateResponse;
import com.partion.comment.dto.CommentListResponse;
import com.partion.comment.dto.CreateCommentRequest;
import com.partion.comment.mapper.CommentMapper;
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
public class CommentService {

    private final CommentMapper commentMapper;
    private final BoardMapper boardMapper;

    @Transactional
    public CommentCreateResponse createComment(Long memberId, Long boardId, CreateCommentRequest request) {
        boardMapper.findById(boardId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BOARD_NOT_FOUND));

        Comment comment = Comment.builder()
                .boardId(boardId)
                .memberId(memberId)
                .content(request.getContent())
                .build();

        commentMapper.insert(comment);

        return new CommentCreateResponse(comment);
    }

    @Transactional(readOnly = true)
    public PageResponse<CommentListResponse> getComments(Long boardId, int page, int size) {
        validatePageRequest(page, size);

        boardMapper.findById(boardId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BOARD_NOT_FOUND));

        int offset = page * size;

        List<CommentListResponse> content = commentMapper.findAllByBoardId(boardId, size, offset)
                .stream()
                .map(CommentListResponse::new)
                .toList();

        long totalElements = commentMapper.countByBoardId(boardId);

        return new PageResponse<>(content, page, size, totalElements);
    }

    private void validatePageRequest(int page, int size) {
        if (page < 0 || size <= 0 || size > 100) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }
}