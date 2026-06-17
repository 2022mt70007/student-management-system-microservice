package com.sms.admin.service;

import com.sms.common.dto.InvitationResponse;
import com.sms.common.enums.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from:noreply@sms.local}")
    private String fromEmail;

    public void sendRegistrationEmail(String to, String name, UserRole role, InvitationResponse invitation) {
        String subject = "Complete your " + role.name().toLowerCase() + " registration";
        String body = """
                Hello %s,

                An administrator has created your %s account.

                Registration link: %s
                Registration code: %s
                Code expires: %s

                Steps:
                1. Open the registration link
                2. Enter your email and the registration code
                3. Set your password

                Regards,
                Student Management System
                """.formatted(name, role.name().toLowerCase(), invitation.getRegistrationLink(),
                invitation.getRegistrationCode(), invitation.getExpiresAt());

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            log.info("Registration email sent to {}", to);
        } catch (Exception ex) {
            log.warn("Failed to send email to {}. Registration link: {}, code: {}. Reason: {}",
                    to, invitation.getRegistrationLink(), invitation.getRegistrationCode(), ex.getMessage());
            log.info("Email body:\n{}", body);
        }
    }
}
