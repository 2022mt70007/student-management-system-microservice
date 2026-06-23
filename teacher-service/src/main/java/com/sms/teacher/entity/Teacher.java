package com.sms.teacher.entity;

import com.sms.common.enums.RegistrationStatus;
import com.sms.common.security.SensitiveStringEncryptor;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "teachers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Teacher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String teacherId;

    @Column(nullable = false, unique = true)
    private String email;

    @Convert(converter = SensitiveStringEncryptor.class)
    private String phone;

    @Convert(converter = SensitiveStringEncryptor.class)
    private String address;

    private Long departmentId;
    private String departmentName;

    private Long classId;
    private String className;

    @ElementCollection
    @CollectionTable(name = "teacher_subject_ids", joinColumns = @JoinColumn(name = "teacher_id"))
    @Column(name = "subject_id")
    @Builder.Default
    private List<Long> subjectIds = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "teacher_subject_names", joinColumns = @JoinColumn(name = "teacher_id"))
    @Column(name = "subject_name")
    @Builder.Default
    private List<String> subjectNames = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private RegistrationStatus status = RegistrationStatus.PENDING_REGISTRATION;
}
