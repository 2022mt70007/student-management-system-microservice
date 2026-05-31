package com.sms.common.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdminUserRequest {
    @NotBlank
    private String name;

    @NotBlank
    private String adminId;

    private String department;

    @NotBlank
    @Email
    private String email;

    private String phone;
    private String address;
}
