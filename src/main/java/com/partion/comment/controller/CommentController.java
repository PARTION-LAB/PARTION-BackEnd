package com.partion.comment.controller;

import com.partion.comment.dto.CommentCreateResponse;
import com.partion.comment.dto.CreateCommentRequest;
import com.partion.comment.service.CommentService;
import com.partion.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/boards/{boardId}/comments")
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<CommentCreateResponse> createComment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long boardId,
            @Valid @RequestBody CreateCommentRequest request
    ) {
        CommentCreateResponse response =
                commentService.createComment(userDetails.getMemberId(), boardId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}