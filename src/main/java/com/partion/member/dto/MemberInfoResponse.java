package com.partion.member.dto;

import com.partion.member.domain.Member;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class MemberInfoResponse {

    private final Long id;
    private final String email;
    private final String nickname;
    private final String provider;
    private final String role;
    private final LocalDateTime createdAt;

    public MemberInfoResponse(Member member) {
        this.id = member.getId();
        this.email = member.getEmail();
        this.nickname = member.getNickname();
        this.provider = member.getProvider();
        this.role = member.getRole();
        this.createdAt = member.getCreatedAt();
    }
}