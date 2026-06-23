package com.sms.course.entity;

import com.sms.common.enums.AcademicStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "departments", uniqueConstraints = {
        @UniqueConstraint(columnNames = "department_code"),
        @UniqueConstraint(columnNames = "department_name")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String departmentCode;

    @Column(nullable = false, unique = true, length = 120)
    private String departmentName;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private AcademicStatus status = AcademicStatus.ACTIVE;
}
