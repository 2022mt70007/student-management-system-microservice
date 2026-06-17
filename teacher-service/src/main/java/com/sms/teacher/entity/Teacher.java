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
    private String department;

    @Column(nullable = false, unique = true)
    private String email;

    @Convert(converter = SensitiveStringEncryptor.class)
    private String phone;
    @Convert(converter = SensitiveStringEncryptor.class)
    private String address;

    @ElementCollection
    @CollectionTable(name = "teacher_subjects", joinColumns = @JoinColumn(name = "teacher_id"))
    @Column(name = "subject")
    @Builder.Default
    private List<String> subjects = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private RegistrationStatus status = RegistrationStatus.PENDING_REGISTRATION;
}
