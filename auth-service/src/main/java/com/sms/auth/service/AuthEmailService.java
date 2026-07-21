package com.sms.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthEmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from:noreply@sms.local}")
    private String fromEmail;

    @Value("${app.password-reset.base-url:http://localhost:5173/reset-password}")
    private String resetBaseUrl;

    public void sendPasswordResetEmail(String to, String code) {
        String link = resetBaseUrl + "?email=" + to + "&code=" + code;
        String body = """
                Hello,

                We received a request to reset your Student Management System password.

                Reset code: %s
                Reset link: %s

                This code expires in 30 minutes. If you did not request a reset, ignore this email.

                Regards,
                Student Management System
                """.formatted(code, link);

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject("Password reset code");
            message.setText(body);
            mailSender.send(message);
            log.info("Password reset email sent to {}", to);
        } catch (Exception ex) {
            log.warn("Failed to send password reset email to {}. Code: {}. Reason: {}",
                    to, code, ex.getMessage());
            log.info("Password reset email body:\n{}", body);
            // Still allow flow in sandbox: code is logged for manual share
        }
    }
}
