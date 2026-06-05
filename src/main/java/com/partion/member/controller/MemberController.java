package com.partion.member.controller;

import com.partion.global.security.CustomUserDetails;
import com.partion.member.dto.MemberInfoResponse;
import com.partion.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/members")
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/me")
    public ResponseEntity<MemberInfoResponse> getMyInfo(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        MemberInfoResponse response = memberService.getMyInfo(userDetails.getMemberId());
        return ResponseEntity.ok(response);
    }
}