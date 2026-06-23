package com.sms.common.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class AcademicSelectionRequest {
    @NotNull
    private Long departmentId;

    @NotNull
    private Long classId;

    @NotEmpty
    private List<Long> subjectIds;
}
