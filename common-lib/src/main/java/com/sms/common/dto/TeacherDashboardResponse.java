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
public class TeacherDashboardResponse {
    private List<SubjectResponse> subjects;
    private List<NotificationResponse> notifications;
    private List<StudentResponse> students;
}
