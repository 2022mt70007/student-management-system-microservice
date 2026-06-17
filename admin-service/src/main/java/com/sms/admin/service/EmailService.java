package com.sms.admin.service;

import com.sms.admin.exception.EmailDeliveryException;
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
            String friendlyMessage = toFriendlyEmailError(to, ex);
            log.warn("Failed to send email to {}. Registration link: {}, code: {}. Reason: {}",
                    to, invitation.getRegistrationLink(), invitation.getRegistrationCode(), ex.getMessage());
            log.info("Email body:\n{}", body);
            throw new EmailDeliveryException(friendlyMessage, ex);
        }
    }

    private String toFriendlyEmailError(String recipient, Exception ex) {
        String raw = ex.getMessage() == null ? "" : ex.getMessage().toLowerCase();

        if (raw.contains("not verified")) {
            if (raw.contains(fromEmail.toLowerCase())) {
                return "Sender email '" + fromEmail + "' is not verified in AWS SES. "
                        + "Verify it under SES → Verified identities.";
            }
            return "Recipient email '" + recipient + "' is not verified in AWS SES. "
                    + "In sandbox mode, verify this address in SES before creating the user.";
        }

        if (raw.contains("invalid") && (raw.contains("address") || raw.contains("recipient"))) {
            return "Invalid recipient email '" + recipient + "'. Check the address and try again.";
        }

        if (raw.contains("authentication failed")) {
            return "Email service authentication failed. Check SMTP credentials and region configuration.";
        }

        if (raw.contains("message rejected")) {
            return "Email provider rejected the message to '" + recipient + "'. "
                    + "Ensure the address is valid and verified in AWS SES.";
        }

        return "Could not send registration email to '" + recipient + "'. "
                + "Use a valid email verified in AWS SES.";
    }
}
