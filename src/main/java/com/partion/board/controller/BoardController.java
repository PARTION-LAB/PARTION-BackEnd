package com.partion.board.controller;

import com.partion.board.dto.*;
import com.partion.board.service.BoardService;
import com.partion.global.response.PageResponse;
import com.partion.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/boards")
public class BoardController {

    private final BoardService boardService;

    @PostMapping
    public ResponseEntity<BoardCreateResponse> createBoard(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CreateBoardRequest request
    ) {
        BoardCreateResponse response =
                boardService.createBoard(userDetails.getMemberId(), request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<PageResponse<BoardListResponse>> getBoards(
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        PageResponse<BoardListResponse> response =
                boardService.getBoards(category, page, size);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{boardId}")
    public ResponseEntity<BoardDetailResponse> getBoardDetail(
            @PathVariable Long boardId
    ) {
        BoardDetailResponse response = boardService.getBoardDetail(boardId);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{boardId}")
    public ResponseEntity<BoardUpdateResponse> updateBoard(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long boardId,
            @Valid @RequestBody UpdateBoardRequest request
    ) {
        BoardUpdateResponse response =
                boardService.updateBoard(userDetails.getMemberId(), boardId, request);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{boardId}")
    public ResponseEntity<Void> deleteBoard(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long boardId
    ) {
        boardService.deleteBoard(userDetails.getMemberId(), boardId);

        return ResponseEntity.noContent().build();
    }
}