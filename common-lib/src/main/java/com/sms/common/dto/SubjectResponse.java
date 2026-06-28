package com.sms.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubjectResponse {
    private Long id;
    private String subjectCode;
    private String subjectName;
    private Long classId;
    private String className;
    private Long departmentId;
    private String departmentName;
    private Integer credits;
    private String description;
    private String teacherName;
}
