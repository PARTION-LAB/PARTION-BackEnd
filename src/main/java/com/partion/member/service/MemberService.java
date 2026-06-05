package com.partion.member.service;

import com.partion.global.exception.BusinessException;
import com.partion.global.exception.ErrorCode;
import com.partion.member.domain.Member;
import com.partion.member.dto.MemberInfoResponse;
import com.partion.member.dto.UpdateMemberRequest;
import com.partion.member.mapper.MemberMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class MemberService {

    private final MemberMapper memberMapper;

    public MemberInfoResponse getMyInfo(Long memberId) {
        Member member = memberMapper.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        return new MemberInfoResponse(member);
    }

    @Transactional
    public MemberInfoResponse updateMyInfo(Long memberId, UpdateMemberRequest request) {
        Member member = memberMapper.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        if (!member.getNickname().equals(request.getNickname())
                && memberMapper.existsByNickname(request.getNickname())) {
            throw new BusinessException(ErrorCode.DUPLICATE_NICKNAME);
        }

        memberMapper.updateNickname(memberId, request.getNickname());

        Member updatedMember = memberMapper.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        return new MemberInfoResponse(updatedMember);
    }
}