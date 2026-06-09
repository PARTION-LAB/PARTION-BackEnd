package com.partion.comment.controller;

import com.partion.comment.service.CommentService;
import com.partion.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/comments")
public class CommentManageController {

    private final CommentService commentService;

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long commentId
    ) {
        commentService.deleteComment(userDetails.getMemberId(), commentId);

        return ResponseEntity.noContent().build();
    }
}