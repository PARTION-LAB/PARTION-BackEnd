package com.partion.member.mapper;

import com.partion.member.domain.Member;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MemberMapper {
    boolean existsByEmail(String email);
    boolean existsByNickname(String nickname);
    void insert(Member member);
}
