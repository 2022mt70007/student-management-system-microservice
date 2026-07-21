package com.sms.auth.service;

import com.sms.auth.client.AdminClient;
import com.sms.auth.client.StudentClient;
import com.sms.auth.client.TeacherClient;
import com.sms.common.dto.ApiResponse;
import com.sms.common.enums.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProfileActivationService {

    private final StudentClient studentClient;
    private final TeacherClient teacherClient;
    private final AdminClient adminClient;

    public void activateProfile(UserRole role, Long profileId) {
        ApiResponse<?> response = switch (role) {
            case STUDENT -> studentClient.activate(profileId);
            case TEACHER -> teacherClient.activate(profileId);
            case ADMIN -> adminClient.activate(profileId);
        };
        if (response == null || !response.isSuccess()) {
            String message = response != null ? response.getMessage() : "No response";
            log.error("Failed to activate {} profile {}: {}", role, profileId, message);
            throw new IllegalStateException("Could not activate profile after registration");
        }
        log.info("Activated {} profile {}", role, profileId);
    }
}
