package com.sms.notification.repository;

import com.sms.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByTargetRoleOrderByCreatedAtDesc(String targetRole);
    Optional<Notification> findFirstByTargetRoleOrderByCreatedAtDesc(String targetRole);
    List<Notification> findAllByOrderByCreatedAtDesc();
}
