package com.partion.auth.service;

import com.partion.auth.dto.EmailVerificationCheckRequest;
import com.partion.auth.dto.EmailVerificationCheckResponse;
import com.partion.auth.dto.EmailVerificationSendRequest;
import com.partion.auth.dto.EmailVerificationSendResponse;
import com.partion.global.exception.BusinessException;
import com.partion.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Set;

@RequiredArgsConstructor
@Service
public class EmailVerificationService {

    private static final long VERIFICATION_CODE_EXPIRE_SECONDS = 300;
    private static final Set<String> ALLOWED_PURPOSES = Set.of("SIGNUP", "PASSWORD_RESET");
    private static final long VERIFIED_EXPIRE_SECONDS = 1800;

    private final JavaMailSender mailSender;
    private final StringRedisTemplate redisTemplate;
    private final SecureRandom secureRandom = new SecureRandom();

    public EmailVerificationSendResponse sendVerificationCode(
            EmailVerificationSendRequest request
    ) {
        validatePurpose(request.getPurpose());

        String code = generateCode();
        String redisKey = buildCodeKey(request.getPurpose(), request.getEmail());

        redisTemplate.opsForValue().set(
                redisKey,
                code,
                Duration.ofSeconds(VERIFICATION_CODE_EXPIRE_SECONDS)
        );

        sendMail(request.getEmail(), request.getPurpose(), code);

        return new EmailVerificationSendResponse(
                request.getEmail(),
                request.getPurpose(),
                VERIFICATION_CODE_EXPIRE_SECONDS
        );
    }

    private void validatePurpose(String purpose) {
        if (!ALLOWED_PURPOSES.contains(purpose)) {
            throw new BusinessException(ErrorCode.INVALID_EMAIL_VERIFICATION_PURPOSE);
        }
    }

    private String generateCode() {
        int number = secureRandom.nextInt(1_000_000);
        return String.format("%06d", number);
    }

    private String buildCodeKey(String purpose, String email) {
        return "email:code:" + purpose + ":" + email;
    }

    private void sendMail(String to, String purpose, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("[Partion] 이메일 인증번호");
        message.setText("""
                Partion 이메일 인증번호입니다.

                인증번호: %s

                이 인증번호는 5분 동안 유효합니다.
                """.formatted(code));

        mailSender.send(message);
    }

    public EmailVerificationCheckResponse verifyCode(
            EmailVerificationCheckRequest request
    ) {
        validatePurpose(request.getPurpose());

        String codeKey = buildCodeKey(request.getPurpose(), request.getEmail());
        String savedCode = redisTemplate.opsForValue().get(codeKey);

        if (savedCode == null) {
            throw new BusinessException(ErrorCode.EMAIL_VERIFICATION_CODE_EXPIRED);
        }

        if (!savedCode.equals(request.getCode())) {
            throw new BusinessException(ErrorCode.EMAIL_VERIFICATION_CODE_MISMATCH);
        }

        String verifiedKey = buildVerifiedKey(request.getPurpose(), request.getEmail());

        redisTemplate.opsForValue().set(
                verifiedKey,
                "true",
                Duration.ofSeconds(VERIFIED_EXPIRE_SECONDS)
        );

        redisTemplate.delete(codeKey);

        return new EmailVerificationCheckResponse(
                request.getEmail(),
                request.getPurpose(),
                true,
                VERIFIED_EXPIRE_SECONDS
        );
    }

    private String buildVerifiedKey(String purpose, String email) {
        return "email:verified:" + purpose + ":" + email;
    }

    public void validateEmailVerified(String purpose, String email) {
        validatePurpose(purpose);

        String verifiedKey = buildVerifiedKey(purpose, email);
        String verified = redisTemplate.opsForValue().get(verifiedKey);

        if (!"true".equals(verified)) {
            throw new BusinessException(ErrorCode.EMAIL_NOT_VERIFIED);
        }
    }

    public void deleteVerifiedEmail(String purpose, String email) {
        validatePurpose(purpose);

        String verifiedKey = buildVerifiedKey(purpose, email);
        redisTemplate.delete(verifiedKey);
    }
}