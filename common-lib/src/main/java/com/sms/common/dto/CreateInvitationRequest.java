package com.sms.common.dto;

import com.sms.common.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateInvitationRequest {
    @NotBlank
    @Email
    private String email;

    @NotNull
    private UserRole role;

    @NotNull
    private Long profileId;

    private String registrationCode;
}
