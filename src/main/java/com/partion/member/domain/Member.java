package com.partion.member.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Member {
    private Long id;
    private String email;
    private String password;
    private String nickname;
    private String provider;
    private String providerId;
    private String role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
