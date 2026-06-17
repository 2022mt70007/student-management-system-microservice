package com.sms.admin.service;

import com.sms.admin.client.AuthClient;
import com.sms.admin.entity.AdminUser;
import com.sms.admin.repository.AdminUserRepository;
import com.sms.common.dto.*;
import com.sms.common.enums.RegistrationStatus;
import com.sms.common.enums.UserRole;
import com.sms.admin.exception.EmailDeliveryException;
import com.sms.common.security.EmailValidator;
import com.sms.common.security.InputSanitizer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminProfileService {

    private final AdminUserRepository adminUserRepository;
    private final AuthClient authClient;
    private final EmailService emailService;

    @Transactional
    public AdminUserResponse create(AdminUserRequest request) {
        String email = InputSanitizer.normalizeEmail(request.getEmail());
        EmailValidator.assertDeliverableRegistrationEmail(email);
        if (adminUserRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Admin with this email already exists");
        }

        AdminUser admin = AdminUser.builder()
                .name(InputSanitizer.cleanText(request.getName()))
                .adminId(InputSanitizer.cleanText(request.getAdminId()))
                .department(InputSanitizer.cleanText(request.getDepartment()))
                .email(email)
                .phone(InputSanitizer.cleanText(request.getPhone()))
                .address(InputSanitizer.cleanText(request.getAddress()))
                .status(RegistrationStatus.PENDING_REGISTRATION)
                .build();

        admin = adminUserRepository.save(admin);

        CreateInvitationRequest invitationRequest = new CreateInvitationRequest();
        invitationRequest.setEmail(admin.getEmail());
        invitationRequest.setRole(UserRole.ADMIN);
        invitationRequest.setProfileId(admin.getId());

        InvitationResponse invitation = authClient.createInvitation(invitationRequest).getData();
        try {
            emailService.sendRegistrationEmail(admin.getEmail(), admin.getName(), UserRole.ADMIN, invitation);
        } catch (EmailDeliveryException ex) {
            throw new IllegalArgumentException(ex.getMessage()
                    + " Admin profile was created. Registration code: " + invitation.getRegistrationCode()
                    + " (share manually).");
        }

        return toResponse(admin);
    }

    @Transactional
    public AdminUserResponse update(Long id, AdminUserRequest request) {
        AdminUser admin = adminUserRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Admin not found"));

        admin.setName(InputSanitizer.cleanText(request.getName()));
        admin.setAdminId(InputSanitizer.cleanText(request.getAdminId()));
        admin.setDepartment(InputSanitizer.cleanText(request.getDepartment()));
        admin.setEmail(InputSanitizer.normalizeEmail(request.getEmail()));
        admin.setPhone(InputSanitizer.cleanText(request.getPhone()));
        admin.setAddress(InputSanitizer.cleanText(request.getAddress()));

        return toResponse(adminUserRepository.save(admin));
    }

    @Transactional
    public void delete(Long id) {
        if (!adminUserRepository.existsById(id)) {
            throw new IllegalArgumentException("Admin not found");
        }
        adminUserRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<AdminUserResponse> findAll() {
        return adminUserRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional
    public void activate(Long profileId) {
        AdminUser admin = adminUserRepository.findById(profileId)
                .orElseThrow(() -> new IllegalArgumentException("Admin not found"));
        admin.setStatus(RegistrationStatus.ACTIVE);
        adminUserRepository.save(admin);
    }

    private AdminUserResponse toResponse(AdminUser admin) {
        return AdminUserResponse.builder()
                .id(admin.getId())
                .name(admin.getName())
                .adminId(admin.getAdminId())
                .department(admin.getDepartment())
                .email(admin.getEmail())
                .phone(admin.getPhone())
                .address(admin.getAddress())
                .status(admin.getStatus())
                .build();
    }
}
