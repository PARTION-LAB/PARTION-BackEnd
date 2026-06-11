package com.partion.auth.service;

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
}