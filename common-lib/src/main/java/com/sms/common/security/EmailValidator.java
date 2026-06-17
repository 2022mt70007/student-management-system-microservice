package com.sms.common.security;

import java.util.Set;
import java.util.regex.Pattern;

public final class EmailValidator {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private static final Set<String> NON_DELIVERABLE_DOMAINS = Set.of(
            "localhost", "invalid", "test", "example", "local"
    );

    private EmailValidator() {
    }

    public static boolean isValidFormat(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * Validates that an email can be used for real registration delivery (e.g. AWS SES).
     */
    public static void assertDeliverableRegistrationEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required.");
        }

        if (!isValidFormat(email)) {
            throw new IllegalArgumentException(
                    "Invalid email format. Use a valid address such as name@gmail.com or name@university.edu.");
        }

        String domain = email.substring(email.indexOf('@') + 1).toLowerCase();
        String tld = domain.contains(".") ? domain.substring(domain.lastIndexOf('.') + 1) : domain;

        if (NON_DELIVERABLE_DOMAINS.contains(tld) || domain.endsWith(".local")) {
            throw new IllegalArgumentException(
                    "Email domain '" + domain + "' cannot receive registration mail. "
                            + "Use a real email address verified in AWS SES.");
        }
    }
}
