package com.sms.notification.config;

import com.sms.notification.entity.Notification;
import com.sms.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

@Configuration
@RequiredArgsConstructor
public class BootstrapConfig {

    private final NotificationRepository notificationRepository;

    @Bean
    CommandLineRunner seedNotifications() {
        return args -> {
            if (notificationRepository.count() == 0) {
                notificationRepository.save(Notification.builder()
                        .title("Welcome to SMS")
                        .message("Welcome to the Student Management System. Check your courses and complete registration.")
                        .targetRole("STUDENT")
                        .createdAt(LocalDateTime.now())
                        .build());
            }
        };
    }
}
