package com.sms.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AcademicSelectionResponse {
    private Long departmentId;
    private String departmentName;
    private Long classId;
    private String className;
    private List<Long> subjectIds;
    private List<String> subjectNames;
}
