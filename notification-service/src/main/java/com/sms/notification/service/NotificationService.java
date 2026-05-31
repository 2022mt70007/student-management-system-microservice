package com.sms.notification.service;

import com.sms.common.dto.NotificationRequest;
import com.sms.common.dto.NotificationResponse;
import com.sms.notification.entity.Notification;
import com.sms.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Transactional
    public NotificationResponse create(NotificationRequest request) {
        Notification notification = Notification.builder()
                .title(request.getTitle())
                .message(request.getMessage())
                .targetRole(request.getTargetRole() != null ? request.getTargetRole().toUpperCase() : "ALL")
                .createdAt(LocalDateTime.now())
                .build();
        return toResponse(notificationRepository.save(notification));
    }

    @Transactional
    public NotificationResponse update(Long id, NotificationRequest request) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found"));

        notification.setTitle(request.getTitle());
        notification.setMessage(request.getMessage());
        notification.setTargetRole(request.getTargetRole() != null ? request.getTargetRole().toUpperCase() : "ALL");

        return toResponse(notificationRepository.save(notification));
    }

    @Transactional
    public void delete(Long id) {
        if (!notificationRepository.existsById(id)) {
            throw new IllegalArgumentException("Notification not found");
        }
        notificationRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> findAll(String role) {
        if (role == null || role.isBlank()) {
            return notificationRepository.findAllByOrderByCreatedAtDesc().stream()
                    .map(this::toResponse).toList();
        }
        return notificationRepository.findByTargetRoleOrderByCreatedAtDesc(role.toUpperCase()).stream()
                .map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public NotificationResponse latest(String role) {
        return notificationRepository.findFirstByTargetRoleOrderByCreatedAtDesc(role.toUpperCase())
                .map(this::toResponse)
                .orElse(null);
    }

    private NotificationResponse toResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .targetRole(notification.getTargetRole())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
