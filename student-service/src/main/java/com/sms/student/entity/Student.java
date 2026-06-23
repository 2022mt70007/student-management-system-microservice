package com.sms.student.entity;

import com.sms.common.enums.RegistrationStatus;
import com.sms.common.security.SensitiveStringEncryptor;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "students")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Convert(converter = SensitiveStringEncryptor.class)
    private String phone;

    @Convert(converter = SensitiveStringEncryptor.class)
    private String address;

    private String rollNumber;

    private Long departmentId;
    private String departmentName;

    private Long classId;
    private String className;

    @ElementCollection
    @CollectionTable(name = "student_subject_ids", joinColumns = @JoinColumn(name = "student_id"))
    @Column(name = "subject_id")
    @Builder.Default
    private List<Long> subjectIds = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "student_subject_names", joinColumns = @JoinColumn(name = "student_id"))
    @Column(name = "subject_name")
    @Builder.Default
    private List<String> subjectNames = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private RegistrationStatus status = RegistrationStatus.PENDING_REGISTRATION;
}
