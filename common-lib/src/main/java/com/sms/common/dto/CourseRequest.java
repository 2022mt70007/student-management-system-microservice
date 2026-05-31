package com.sms.common.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CourseRequest {
    @NotBlank
    private String title;

    private String description;
    private String department;
    private String instructor;
    private Integer credits;
}
