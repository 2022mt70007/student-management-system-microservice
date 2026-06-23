package com.sms.common.dto;

import com.sms.common.enums.AcademicStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentResponse {
    private Long id;
    private String departmentCode;
    private String departmentName;
    private String description;
    private AcademicStatus status;
}
