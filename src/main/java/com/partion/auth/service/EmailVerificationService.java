package com.partion.auth.service;

import com.partion.auth.dto.EmailVerificationSendRequest;
import com.partion.auth.dto.EmailVerificationSendResponse;
import com.partion.global.exception.BusinessException;
import com.partion.global.exception.ErrorCode;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Set;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class EmailVerificationService {

    private static final long VERIFICATION_LINK_EXPIRE_SECONDS = 300;
    private static final Set<String> ALLOWED_PURPOSES = Set.of("SIGNUP", "PASSWORD_RESET");
    private static final long VERIFIED_EXPIRE_SECONDS = 1800;

    private final JavaMailSender mailSender;
    private final StringRedisTemplate redisTemplate;

    @Value("${app.backend-base-url}")
    private String backendBaseUrl;

    @Value("${app.frontend-base-url}")
    private String frontendBaseUrl;

    public EmailVerificationSendResponse sendVerificationCode(
            EmailVerificationSendRequest request
    ) {
        validatePurpose(request.getPurpose());

        String token = generateToken();
        String redisKey = buildLinkTokenKey(request.getPurpose(), token);

        redisTemplate.opsForValue().set(
                redisKey,
                request.getEmail(),
                Duration.ofSeconds(VERIFICATION_LINK_EXPIRE_SECONDS)
        );

        String verificationUrl = createVerificationUrl(request.getPurpose(), token);

        sendMail(request.getEmail(), request.getPurpose(), verificationUrl);

        return new EmailVerificationSendResponse(
                request.getEmail(),
                request.getPurpose(),
                VERIFICATION_LINK_EXPIRE_SECONDS
        );
    }

    private void validatePurpose(String purpose) {
        if (!ALLOWED_PURPOSES.contains(purpose)) {
            throw new BusinessException(ErrorCode.INVALID_EMAIL_VERIFICATION_PURPOSE);
        }
    }

    private String generateToken() {
        return UUID.randomUUID().toString();
    }

    private String buildLinkTokenKey(String purpose, String token) {
        return "email:link:" + purpose + ":" + token;
    }

    private String createVerificationUrl(String purpose, String token) {
        return backendBaseUrl
                + "/api/auth/email/verify-link"
                + "?purpose=" + encode(purpose)
                + "&token=" + encode(token);
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private void sendMail(String to, String purpose, String verificationUrl) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");

            helper.setTo(to);
            helper.setSubject(createMailSubject(purpose));
            helper.setText(createMailHtml(purpose, verificationUrl), true);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    private String createMailHtml(String purpose, String verificationUrl) {
        String title = "PASSWORD_RESET".equals(purpose)
                ? "비밀번호 재설정을 위한 이메일 인증입니다."
                : "Partion 회원가입 이메일 인증입니다.";

        String description = "PASSWORD_RESET".equals(purpose)
                ? "비밀번호 재설정을 계속하려면 아래 버튼을 눌러주세요."
                : "회원가입을 완료하려면 아래 버튼을 눌러주세요.";

        return """
            <!DOCTYPE html>
            <html>
            <body style="margin:0; padding:0; background-color:#f5f6f8; font-family:Arial, sans-serif;">
              <div style="max-width:600px; margin:40px auto; background-color:#ffffff; border-radius:8px; overflow:hidden; border:1px solid #e5e7eb;">
                <div style="background-color:#163b73; padding:28px;">
                  <h1 style="margin:0; color:#ffffff; font-size:24px;">Partion 이메일 인증</h1>
                </div>
                <div style="padding:36px; text-align:center;">
                  <h2 style="color:#222222; font-size:20px;">%s</h2>
                  <p style="color:#555555; line-height:1.6;">%s</p>
                  <a href="%s" style="display:inline-block; margin-top:24px; padding:14px 28px; background-color:#3478f6; color:#ffffff; text-decoration:none; border-radius:6px; font-weight:bold;">
                    이메일 인증하기
                  </a>
                  <p style="margin-top:32px; color:#999999; font-size:13px;">
                    이 링크는 5분 동안 유효합니다.
                  </p>
                </div>
              </div>
            </body>
            </html>
            """.formatted(title, description, verificationUrl);
    }

    private String createMailSubject(String purpose) {
        if ("PASSWORD_RESET".equals(purpose)) {
            return "[Partion] 비밀번호 재설정 이메일 인증";
        }

        return "[Partion] 회원가입 이메일 인증";
    }

    public String verifyEmailLink(String purpose, String token) {
        validatePurpose(purpose);

        String linkTokenKey = buildLinkTokenKey(purpose, token);
        String email = redisTemplate.opsForValue().get(linkTokenKey);

        if (email == null) {
            return createRedirectUrl(purpose, false, null, "expired");
        }

        String verifiedKey = buildVerifiedKey(purpose, email);

        redisTemplate.opsForValue().set(
                verifiedKey,
                "true",
                Duration.ofSeconds(VERIFIED_EXPIRE_SECONDS)
        );

        redisTemplate.delete(linkTokenKey);

        return createRedirectUrl(purpose, true, email, null);
    }

    private String createRedirectUrl(String purpose, boolean success, String email, String reason) {
        String path = "PASSWORD_RESET".equals(purpose)
                ? "/password-reset"
                : "/signup";

        if (!success) {
            return frontendBaseUrl
                    + path
                    + "?emailVerified=false"
                    + "&reason=" + encode(reason);
        }

        return frontendBaseUrl
                + path
                + "?emailVerified=true"
                + "&email=" + encode(email);
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