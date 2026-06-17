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
public class StudentEnrolledCourseResponse {
    private Long id;
    private String title;
    private String description;
    private String department;
    private String instructor;
    private Integer credits;
    private Integer progressPercent;
    private List<AssignmentResponse> assignments;
    private List<ExamResponse> exams;
}
