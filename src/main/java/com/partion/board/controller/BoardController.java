package com.partion.board.controller;

import com.partion.board.dto.BoardCreateResponse;
import com.partion.board.dto.CreateBoardRequest;
import com.partion.board.service.BoardService;
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
}