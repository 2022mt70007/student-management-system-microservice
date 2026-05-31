package com.sms.auth.entity;

import com.sms.common.enums.UserRole;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "registration_invitations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistrationInvitation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    private Long profileId;

    @Builder.Default
    private boolean used = false;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    private LocalDateTime createdAt;
}
