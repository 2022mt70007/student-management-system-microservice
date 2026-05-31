package com.sms.admin.config;

import com.sms.admin.repository.AdminUserRepository;
import com.sms.admin.service.AdminProfileService;
import com.sms.common.dto.AdminUserRequest;
import com.sms.common.enums.RegistrationStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class BootstrapConfig {

    private static final int MAX_ATTEMPTS = 12;
    private static final long RETRY_DELAY_MS = 5000;

    private final AdminUserRepository adminUserRepository;
    private final AdminProfileService adminProfileService;

    @EventListener(ApplicationReadyEvent.class)
    public void seedBootstrapAdmin() {
        if (adminUserRepository.count() > 0) {
            return;
        }

        AdminUserRequest request = new AdminUserRequest();
        request.setName("System Admin");
        request.setAdminId("ADM001");
        request.setDepartment("Administration");
        request.setEmail("admin@sms.local");
        request.setPhone("0000000000");
        request.setAddress("Head Office");

        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                log.info("Seeding bootstrap admin user (attempt {}/{})", attempt, MAX_ATTEMPTS);
                adminProfileService.create(request);

                adminUserRepository.findByEmail("admin@sms.local").ifPresent(admin -> {
                    admin.setStatus(RegistrationStatus.PENDING_REGISTRATION);
                    adminUserRepository.save(admin);
                });

                log.info("Bootstrap admin created for admin@sms.local. Check MailHog or logs for registration code.");
                return;
            } catch (Exception ex) {
                log.warn("Bootstrap admin seed failed (attempt {}/{}): {}", attempt, MAX_ATTEMPTS, ex.getMessage());
                if (attempt == MAX_ATTEMPTS) {
                    log.error("Could not seed bootstrap admin. Start auth-service and restart admin-service.");
                    return;
                }
                sleep(RETRY_DELAY_MS);
            }
        }
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
