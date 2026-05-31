package com.sms.common.dto;

import com.sms.common.enums.RegistrationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeacherResponse {
    private Long id;
    private String name;
    private String teacherId;
    private String department;
    private String email;
    private String phone;
    private String address;
    private List<String> subjects;
    private RegistrationStatus status;
}
