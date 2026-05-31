package com.sms.common.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class NotificationRequest {
    @NotBlank
    private String title;

    @NotBlank
    private String message;

    private String targetRole;
}
