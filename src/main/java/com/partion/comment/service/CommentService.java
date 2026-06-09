package com.partion.comment.service;

import com.partion.board.mapper.BoardMapper;
import com.partion.comment.domain.Comment;
import com.partion.comment.dto.CommentCreateResponse;
import com.partion.comment.dto.CreateCommentRequest;
import com.partion.comment.mapper.CommentMapper;
import com.partion.global.exception.BusinessException;
import com.partion.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}