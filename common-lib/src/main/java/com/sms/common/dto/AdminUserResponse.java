package com.sms.common.dto;

import com.sms.common.enums.RegistrationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserResponse {
    private Long id;
    private String name;
    private String adminId;
    private String department;
    private String email;
    private String phone;
    private String address;
    private RegistrationStatus status;
}
