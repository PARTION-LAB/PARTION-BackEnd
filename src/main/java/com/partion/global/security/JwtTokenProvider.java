package com.partion.global.security;

import com.partion.global.exception.BusinessException;
import com.partion.global.exception.ErrorCode;
import com.partion.member.domain.Member;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;

    public String createAccessToken(Member member) {
        return createToken(member, jwtProperties.getAccessTokenExpiration());
    }

    public String createRefreshToken(Member member) {
        return createToken(member, jwtProperties.getRefreshTokenExpiration());
    }

    private String createToken(Member member, long expirationMillis) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + expirationMillis);

        return Jwts.builder()
                .subject(String.valueOf(member.getId()))
                .claim("email", member.getEmail())
                .claim("role", member.getRole())
                .issuedAt(now)
                .expiration(expiration)
                .signWith(getSecretKey())
                .compact();
    }

    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public long getAccessTokenExpirationSeconds() {
        return jwtProperties.getAccessTokenExpiration() / 1000;
    }

    public long getRefreshTokenExpirationMillis() {
        return jwtProperties.getRefreshTokenExpiration();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSecretKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException exception) {
            return false;
        }
    }

    public Long getMemberId(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSecretKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return Long.valueOf(claims.getSubject());
        } catch (JwtException | IllegalArgumentException exception) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }
    }

    public long getRemainingExpiration(String token) {
        try {
            Date expiration = Jwts.parser()
                    .verifyWith(getSecretKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getExpiration();

            long now = System.currentTimeMillis();
            return expiration.getTime() - now;
        } catch (JwtException | IllegalArgumentException exception) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }
    }
}
