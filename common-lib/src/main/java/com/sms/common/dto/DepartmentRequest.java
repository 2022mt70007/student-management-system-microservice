package com.sms.common.dto;

import com.sms.common.enums.AcademicStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DepartmentRequest {
    @NotBlank
    @Size(max = 20)
    private String departmentCode;

    @NotBlank
    @Size(max = 120)
    private String departmentName;

    @Size(max = 500)
    private String description;

    @NotNull
    private AcademicStatus status;
}
