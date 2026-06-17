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
public class AssignmentResponse {
    private Long id;
    private Long courseId;
    private String title;
    private String description;
    private LocalDate dueDate;
    /** OVERDUE, DUE_SOON, or UPCOMING */
    private String priorityStatus;
}
