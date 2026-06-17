package com.sms.auth.entity;

import com.sms.common.enums.UserRole;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    private Long profileId;

    @Builder.Default
    private boolean enabled = false;

    @Column(nullable = false)
    @ColumnDefault("0")
    @Builder.Default
    private int failedLoginAttempts = 0;

    private LocalDateTime accountLockedUntil;

    private LocalDateTime createdAt;
}
