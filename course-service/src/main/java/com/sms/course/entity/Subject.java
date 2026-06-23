package com.sms.course.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "subjects", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"class_id", "subject_code"}),
        @UniqueConstraint(columnNames = {"class_id", "subject_name"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String subjectCode;

    @Column(nullable = false, length = 120)
    private String subjectName;

    @Column(nullable = false)
    private Long classId;

    private Integer credits;

    @Column(length = 500)
    private String description;
}
