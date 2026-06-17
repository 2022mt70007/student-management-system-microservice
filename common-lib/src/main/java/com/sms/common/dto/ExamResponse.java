package com.sms.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamResponse {
    private Long id;
    private Long courseId;
    private String title;
    private String description;
    private LocalDate scheduledDate;
    /** EXAM_SOON or UPCOMING */
    private String priorityStatus;
}
