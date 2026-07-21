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
public class StudentDashboardResponse {
    private String className;
    private String departmentName;
    private String rollNumber;
    private int subjectCount;
    private List<SubjectResponse> subjects;
    private NotificationResponse latestNotification;
}
