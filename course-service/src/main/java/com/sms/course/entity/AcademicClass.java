package com.sms.course.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "academic_classes", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"department_id", "class_code"}),
        @UniqueConstraint(columnNames = {"department_id", "class_name"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AcademicClass {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String classCode;

    @Column(nullable = false, length = 120)
    private String className;

    @Column(nullable = false)
    private Long departmentId;

    @Column(length = 500)
    private String description;
}
