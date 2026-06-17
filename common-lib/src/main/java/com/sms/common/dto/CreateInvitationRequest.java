package com.sms.common.dto;

import com.sms.common.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateInvitationRequest {
    @NotBlank
    @Email
    @Size(max = 120)
    private String email;

    @NotNull
    private UserRole role;

    @NotNull
    private Long profileId;

    @Pattern(regexp = "^$|\\d{6,7}", message = "Code must be 6 or 7 digits")
    private String registrationCode;
}
