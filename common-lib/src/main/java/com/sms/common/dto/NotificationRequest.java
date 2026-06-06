package com.sms.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class NotificationRequest {
    @NotBlank
    @Size(max = 120)
    private String title;

    @NotBlank
    @Size(max = 1000)
    private String message;

    @Pattern(regexp = "^$|ADMIN|TEACHER|STUDENT|ALL", message = "Target role must be ADMIN, TEACHER, STUDENT, or ALL")
    private String targetRole;
}
