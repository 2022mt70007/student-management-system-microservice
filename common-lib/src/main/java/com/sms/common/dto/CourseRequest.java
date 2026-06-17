package com.sms.common.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CourseRequest {
    @NotBlank
    @Size(max = 120)
    private String title;

    @Size(max = 1000)
    private String description;
    @Size(max = 80)
    private String department;
    @Size(max = 100)
    private String instructor;

    @Min(value = 1, message = "Credits must be at least 1")
    @Max(value = 10, message = "Credits cannot exceed 10")
    private Integer credits;
}
