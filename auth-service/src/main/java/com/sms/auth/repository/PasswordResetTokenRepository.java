package com.sms.auth.repository;

import com.sms.auth.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByEmailAndCodeAndUsedFalse(String email, String code);

    void deleteByEmail(String email);
}
