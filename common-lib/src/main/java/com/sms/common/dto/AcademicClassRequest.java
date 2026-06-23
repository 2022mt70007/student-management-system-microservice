package com.sms.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AcademicClassRequest {
    @NotBlank
    @Size(max = 20)
    private String classCode;

    @NotBlank
    @Size(max = 120)
    private String className;

    @NotNull
    private Long departmentId;

    @Size(max = 500)
    private String description;
}
