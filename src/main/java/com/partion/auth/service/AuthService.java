package com.partion.auth.service;

import com.partion.auth.dto.*;
import com.partion.global.security.JwtTokenProvider;
import com.partion.member.domain.Member;
import com.partion.member.mapper.MemberMapper;
import com.partion.wallet.domain.Wallet;
import com.partion.wallet.mapper.WalletMapper;
import com.partion.global.exception.BusinessException;
import com.partion.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Service
@Transactional
public class AuthService {
    private final MemberMapper memberMapper;
    private final WalletMapper walletMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final StringRedisTemplate stringRedisTemplate;
    private final EmailVerificationService emailVerificationService;

    public SignupResponse signup(SignupRequest request) {
        if(memberMapper.existsByEmail(request.getEmail())) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }

        if(memberMapper.existsByNickname(request.getNickname())) {
            throw new BusinessException(ErrorCode.DUPLICATE_NICKNAME);
        }

        emailVerificationService.validateEmailVerified("SIGNUP", request.getEmail());

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

        emailVerificationService.deleteVerifiedEmail("SIGNUP", request.getEmail());

        return new SignupResponse(
                member.getId(),
                member.getEmail(),
                member.getNickname()
        );
    }

    public TokenResponse login(LoginRequest request) {
        Member member = memberMapper.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_LOGIN_REQUEST));

        if(!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_LOGIN_REQUEST);
        }

        String accessToken = jwtTokenProvider.createAccessToken(member);
        String refreshToken = jwtTokenProvider.createRefreshToken(member);

        // Redis에 Refresh Token 저장
        // key -> refresh: {memberId}
        // value -> refreshToken 문자열
        // TTL -> jwt.refresh-token-expiration
        stringRedisTemplate.opsForValue().set(
                "refresh:" + member.getId(),
                refreshToken,
                Duration.ofMillis(jwtTokenProvider.getRefreshTokenExpirationMillis())
        );

        return new TokenResponse(
                accessToken,
                refreshToken,
                "Bearer",
                jwtTokenProvider.getAccessTokenExpirationSeconds()
        );
    }

    public AccessTokenResponse reissue(ReissueRequest request) {
        String refreshToken = request.getRefreshToken();

        if(!jwtTokenProvider.validateToken(refreshToken)) {
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        Long memberId = jwtTokenProvider.getMemberId(refreshToken);

        String savedRefreshToken = stringRedisTemplate.opsForValue().get("refresh:" + memberId);

        if(savedRefreshToken == null) {
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        if(!savedRefreshToken.equals(refreshToken)) {
            throw new BusinessException(ErrorCode.REFRESH_TOKEN_MISMATCH);
        }

        Member member = memberMapper.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        String newAccessToken = jwtTokenProvider.createAccessToken(member);

        return new AccessTokenResponse(
                newAccessToken,
                "Bearer",
                jwtTokenProvider.getAccessTokenExpirationSeconds()
        );
    }

    @Transactional
    public void logout(String accessToken) {
        if(!jwtTokenProvider.validateToken(accessToken)) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

        Long memberId = jwtTokenProvider.getMemberId(accessToken);

        stringRedisTemplate.delete("refresh:" + memberId);

        long remainingExpiration = jwtTokenProvider.getRemainingExpiration(accessToken);

        if(remainingExpiration > 0) {
            stringRedisTemplate.opsForValue().set(
                    "blacklist:" + accessToken,
                    "logout",
                    remainingExpiration,
                    TimeUnit.MILLISECONDS
            );
        }
    }
}
