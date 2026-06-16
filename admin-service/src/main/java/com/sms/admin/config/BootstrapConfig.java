package com.sms.admin.config;

import com.sms.admin.repository.AdminUserRepository;
import com.sms.admin.service.AdminProfileService;
import com.sms.common.dto.AdminUserRequest;
import com.sms.common.enums.RegistrationStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
@Slf4j
public class BootstrapConfig {

    private static final int MAX_ATTEMPTS = 12;
    private static final long RETRY_DELAY_MS = 5000;

    private final AdminUserRepository adminUserRepository;
    private final AdminProfileService adminProfileService;

    @Value("${app.bootstrap.admin-email:}")
    private String bootstrapAdminEmail;

    @EventListener(ApplicationReadyEvent.class)
    public void seedBootstrapAdmin() {
        if (adminUserRepository.count() > 0) {
            return;
        }

        if (!StringUtils.hasText(bootstrapAdminEmail)) {
            log.warn("Skipping bootstrap admin seed: set BOOTSTRAP_ADMIN_EMAIL or MAIL_FROM to a SES-verified address.");
            return;
        }

        String email = bootstrapAdminEmail.trim().toLowerCase();

        AdminUserRequest request = new AdminUserRequest();
        request.setName("System Admin");
        request.setAdminId("ADM001");
        request.setDepartment("Administration");
        request.setEmail(email);
        request.setPhone("0000000000");
        request.setAddress("Head Office");

        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                log.info("Seeding bootstrap admin user (attempt {}/{})", attempt, MAX_ATTEMPTS);
                adminProfileService.create(request);

                adminUserRepository.findByEmail(email).ifPresent(admin -> {
                    admin.setStatus(RegistrationStatus.PENDING_REGISTRATION);
                    adminUserRepository.save(admin);
                });

                log.info("Bootstrap admin created for {}. Check email or logs for registration code.", email);
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
