package com.sms.common.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SetPasswordRequest {
    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Pattern(regexp = "\\d{6,7}", message = "Code must be 6 or 7 digits")
    private String code;

    @NotBlank
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;
}
