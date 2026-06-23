package com.sms.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SubjectRequest {
    @NotBlank
    @Size(max = 20)
    private String subjectCode;

    @NotBlank
    @Size(max = 120)
    private String subjectName;

    @NotNull
    private Long classId;

    private Integer credits;

    @Size(max = 500)
    private String description;
}
