package com.sms.common.dto;

import com.sms.common.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvitationResponse {
    private String email;
    private UserRole role;
    private Long profileId;
    private String registrationCode;
    private String registrationLink;
    private LocalDateTime expiresAt;
}
