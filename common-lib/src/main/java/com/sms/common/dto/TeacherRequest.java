package com.sms.common.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class TeacherRequest {
    @NotBlank
    private String name;

    @NotBlank
    private String teacherId;

    private String department;

    @NotBlank
    @Email
    private String email;

    private String phone;
    private String address;
    private List<String> subjects;
}
