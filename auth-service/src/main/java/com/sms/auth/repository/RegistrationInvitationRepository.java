package com.sms.auth.repository;

import com.sms.auth.entity.RegistrationInvitation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RegistrationInvitationRepository extends JpaRepository<RegistrationInvitation, Long> {
    Optional<RegistrationInvitation> findByEmailAndCode(String email, String code);
    Optional<RegistrationInvitation> findByEmail(String email);
}
