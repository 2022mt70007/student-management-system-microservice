package com.sms.admin.service;

import com.sms.admin.client.AuthClient;
import com.sms.admin.entity.AdminUser;
import com.sms.admin.repository.AdminUserRepository;
import com.sms.common.dto.*;
import com.sms.common.enums.RegistrationStatus;
import com.sms.common.enums.UserRole;
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
        if (adminUserRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Admin with this email already exists");
        }

        AdminUser admin = AdminUser.builder()
                .name(request.getName())
                .adminId(request.getAdminId())
                .department(request.getDepartment())
                .email(request.getEmail())
                .phone(request.getPhone())
                .address(request.getAddress())
                .status(RegistrationStatus.PENDING_REGISTRATION)
                .build();

        admin = adminUserRepository.save(admin);

        CreateInvitationRequest invitationRequest = new CreateInvitationRequest();
        invitationRequest.setEmail(admin.getEmail());
        invitationRequest.setRole(UserRole.ADMIN);
        invitationRequest.setProfileId(admin.getId());

        InvitationResponse invitation = authClient.createInvitation(invitationRequest).getData();
        emailService.sendRegistrationEmail(admin.getEmail(), admin.getName(), UserRole.ADMIN, invitation);

        return toResponse(admin);
    }

    @Transactional
    public AdminUserResponse update(Long id, AdminUserRequest request) {
        AdminUser admin = adminUserRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Admin not found"));

        admin.setName(request.getName());
        admin.setAdminId(request.getAdminId());
        admin.setDepartment(request.getDepartment());
        admin.setEmail(request.getEmail());
        admin.setPhone(request.getPhone());
        admin.setAddress(request.getAddress());

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
