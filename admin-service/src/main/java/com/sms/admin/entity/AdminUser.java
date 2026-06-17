package com.sms.admin.entity;

import com.sms.common.enums.RegistrationStatus;
import com.sms.common.security.SensitiveStringEncryptor;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "admins")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String adminId;
    private String department;

    @Column(nullable = false, unique = true)
    private String email;

    @Convert(converter = SensitiveStringEncryptor.class)
    private String phone;
    @Convert(converter = SensitiveStringEncryptor.class)
    private String address;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private RegistrationStatus status = RegistrationStatus.PENDING_REGISTRATION;
}
