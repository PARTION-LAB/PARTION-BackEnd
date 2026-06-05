package com.partion.member.controller;

import com.partion.global.security.CustomUserDetails;
import com.partion.member.dto.ChangePasswordRequest;
import com.partion.member.dto.MemberInfoResponse;
import com.partion.member.dto.UpdateMemberRequest;
import com.partion.member.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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

    @PatchMapping("/me")
    public ResponseEntity<MemberInfoResponse> updateMyInfo(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UpdateMemberRequest request
    ) {
        MemberInfoResponse response =
                memberService.updateMyInfo(userDetails.getMemberId(), request);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/me/password")
    public ResponseEntity<Void> changePassword(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        memberService.changePassword(userDetails.getMemberId(), request);
        return ResponseEntity.noContent().build();
    }
}