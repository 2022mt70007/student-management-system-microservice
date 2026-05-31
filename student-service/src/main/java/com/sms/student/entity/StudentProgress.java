package com.sms.student.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "student_progress")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long studentId;
    private Long courseId;
    private String courseTitle;

    @Builder.Default
    private Integer progressPercent = 0;
}
