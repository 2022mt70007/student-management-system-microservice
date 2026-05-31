package com.sms.common.dto;

import com.sms.common.enums.RegistrationStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class StudentRequest {
    @NotBlank
    private String name;

    @NotBlank
    @Email
    private String email;

    private String phone;
    private String address;
    private String rollNumber;
    private String className;
    private String department;
    private List<String> subjects;
}
