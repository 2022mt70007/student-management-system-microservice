package com.sms.common.security;

import java.util.ArrayList;
import java.util.List;

public final class InputSanitizer {

    private InputSanitizer() {
    }

    public static String cleanText(String value) {
        if (value == null) {
            return null;
        }
        String sanitized = value
                .replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "")
                .trim()
                .replaceAll("\\s{2,}", " ");
        return sanitized.isEmpty() ? null : sanitized;
    }

    public static String normalizeEmail(String email) {
        String cleaned = cleanText(email);
        return cleaned == null ? null : cleaned.toLowerCase();
    }

    public static List<String> cleanList(List<String> values) {
        if (values == null) {
            return List.of();
        }
        List<String> cleaned = new ArrayList<>();
        for (String value : values) {
            String sanitized = cleanText(value);
            if (sanitized != null) {
                cleaned.add(sanitized);
            }
        }
        return cleaned;
    }
}
