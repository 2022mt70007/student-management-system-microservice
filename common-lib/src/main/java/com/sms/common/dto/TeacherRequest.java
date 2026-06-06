package com.sms.common.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class TeacherRequest {
    @NotBlank
    @Size(max = 100)
    private String name;

    @NotBlank
    @Size(max = 30)
    private String teacherId;

    @Size(max = 80)
    private String department;

    @NotBlank
    @Email
    @Size(max = 120)
    private String email;

    @Pattern(regexp = "^$|^[+]?\\d{10,15}$", message = "Phone must be 10-15 digits")
    private String phone;
    @Size(max = 255)
    private String address;
    private List<@NotBlank @Size(max = 80) String> subjects;
}
