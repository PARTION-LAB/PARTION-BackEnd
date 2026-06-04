package com.partion.auth.service;

import com.partion.auth.dto.SignupRequest;
import com.partion.auth.dto.SignupResponse;
import com.partion.member.domain.Member;
import com.partion.member.mapper.MemberMapper;
import com.partion.wallet.domain.Wallet;
import com.partion.wallet.mapper.WalletMapper;
import com.partion.global.exception.BusinessException;
import com.partion.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@RequiredArgsConstructor
@Service
@Transactional
public class AuthService {
    private final MemberMapper memberMapper;
    private final WalletMapper walletMapper;
    private final PasswordEncoder passwordEncoder;

    public SignupResponse signup(SignupRequest request) {
        if(memberMapper.existsByEmail(request.getEmail())) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }

        if(memberMapper.existsByNickname(request.getNickname())) {
            throw new BusinessException(ErrorCode.DUPLICATE_NICKNAME);
        }

        String encodePassword = passwordEncoder.encode(request.getPassword());

        Member member = Member.builder()
                .email(request.getEmail())
                .password(encodePassword)
                .nickname(request.getNickname())
                .provider("LOCAL")
                .role("USER")
                .build();

        memberMapper.insert(member);

        Wallet wallet = Wallet.builder()
                .memberId(member.getId())
                .availableBalance(BigDecimal.ZERO)
                .lockedBalance(BigDecimal.ZERO)
                .build();

        walletMapper.insert(wallet);

        return new SignupResponse(
                member.getId(),
                member.getEmail(),
                member.getNickname()
        );
    }
}
