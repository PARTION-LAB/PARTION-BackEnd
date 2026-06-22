package com.partion.auth.service;

import com.partion.auth.dto.*;
import com.partion.global.security.JwtTokenProvider;
import com.partion.member.domain.Member;
import com.partion.member.dto.MemberInfoResponse;
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
import java.util.Optional;
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

    public TokenIssueResult login(LoginRequest request) {
        Member member = memberMapper.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_LOGIN_REQUEST));

        if (!"LOCAL".equals(member.getProvider()) || member.getPassword() == null) {
            throw new BusinessException(ErrorCode.INVALID_LOGIN_REQUEST);
        }

        if(!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_LOGIN_REQUEST);
        }

        return issueTokens(member);
    }

    public AccessTokenResponse reissue(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        Long memberId = jwtTokenProvider.getMemberId(refreshToken);

        String savedRefreshToken = stringRedisTemplate.opsForValue().get("refresh:" + memberId);

        if (savedRefreshToken == null) {
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        if (!savedRefreshToken.equals(refreshToken)) {
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

    public PasswordResetResponse resetPassword(PasswordResetRequest request) {
        Member member = memberMapper.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        emailVerificationService.validateEmailVerified("PASSWORD_RESET", request.getEmail());

        String encodedPassword = passwordEncoder.encode(request.getNewPassword());

        memberMapper.updatePasswordByEmail(member.getEmail(), encodedPassword);

        emailVerificationService.deleteVerifiedEmail("PASSWORD_RESET", request.getEmail());

        return new PasswordResetResponse(member.getEmail());
    }

    private Member createOAuthMember(OAuthUserInfo userInfo) {
        if (memberMapper.existsByEmail(userInfo.getEmail())) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }

        String nickname = createOAuthNickname(userInfo);

        Member member = Member.builder()
                .email(userInfo.getEmail())
                .password(null)
                .nickname(nickname)
                .provider(userInfo.getProvider())
                .providerId(userInfo.getProviderId())
                .role("USER")
                .build();

        memberMapper.insert(member);

        Wallet wallet = Wallet.builder()
                .memberId(member.getId())
                .availableBalance(BigDecimal.ZERO)
                .lockedBalance(BigDecimal.ZERO)
                .build();

        walletMapper.insert(wallet);

        return member;
    }

    private String createOAuthNickname(OAuthUserInfo userInfo) {
        String base = userInfo.getNickname();

        if (base == null || base.isBlank()) {
            base = userInfo.getProvider().toLowerCase() + "_" + userInfo.getProviderId();
        }

        base = base.replaceAll("[^가-힣a-zA-Z0-9_]", "");

        if (base.length() > 20) {
            base = base.substring(0, 20);
        }

        String nickname = base;
        int suffix = 1;

        while (memberMapper.existsByNickname(nickname)) {
            String nextSuffix = String.valueOf(suffix++);
            int maxBaseLength = Math.max(1, 20 - nextSuffix.length());
            nickname = base.substring(0, Math.min(base.length(), maxBaseLength)) + nextSuffix;
        }

        return nickname;
    }

    public TokenIssueResult oauthLogin(OAuthUserInfo userInfo) {
        Member member = findExistingOAuthMember(userInfo)
                .orElseGet(() -> createOAuthMember(userInfo));

        return issueTokens(member);
    }

    private Optional<Member> findExistingOAuthMember(OAuthUserInfo userInfo) {
        Optional<Member> member = memberMapper.findByProviderAndProviderId(
                userInfo.getProvider(),
                userInfo.getProviderId()
        );

        if (member.isPresent()) {
            return member;
        }

        if (userInfo.getEmail() == null || userInfo.getEmail().isBlank()) {
            return Optional.empty();
        }

        return memberMapper.findByEmail(userInfo.getEmail());
    }

    private TokenIssueResult issueTokens(Member member) {
        String accessToken = jwtTokenProvider.createAccessToken(member);
        String refreshToken = jwtTokenProvider.createRefreshToken(member);

        stringRedisTemplate.opsForValue().set(
                "refresh:" + member.getId(),
                refreshToken,
                Duration.ofMillis(jwtTokenProvider.getRefreshTokenExpirationMillis())
        );

        return new TokenIssueResult(
                accessToken,
                refreshToken,
                "Bearer",
                jwtTokenProvider.getAccessTokenExpirationSeconds(),
                jwtTokenProvider.getRefreshTokenExpirationMillis() / 1000,
                new MemberInfoResponse(member)
        );
    }
}
