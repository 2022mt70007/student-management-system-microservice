package com.sms.auth.service;

import com.sms.auth.entity.RegistrationInvitation;
import com.sms.auth.entity.User;
import com.sms.auth.repository.RegistrationInvitationRepository;
import com.sms.auth.repository.UserRepository;
import com.sms.common.dto.*;
import com.sms.common.enums.UserRole;
import com.sms.common.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RegistrationInvitationRepository invitationRepository;
    private final PasswordEncoder passwordEncoder;
    private final ProfileActivationService profileActivationService;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration-ms:86400000}")
    private long jwtExpirationMs;

    @Value("${app.registration.base-url:http://localhost:8080/register}")
    private String registrationBaseUrl;

    @Value("${app.registration.code-expiry-days:7}")
    private int codeExpiryDays;

    @Transactional
    public InvitationResponse createInvitation(CreateInvitationRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("User already registered with this email");
        }

        String code = request.getRegistrationCode() != null && !request.getRegistrationCode().isBlank()
                ? request.getRegistrationCode()
                : generateCode();

        invitationRepository.findByEmail(request.getEmail()).ifPresent(invitationRepository::delete);

        RegistrationInvitation invitation = RegistrationInvitation.builder()
                .email(request.getEmail())
                .code(code)
                .role(request.getRole())
                .profileId(request.getProfileId())
                .used(false)
                .expiresAt(LocalDateTime.now().plusDays(codeExpiryDays))
                .createdAt(LocalDateTime.now())
                .build();

        invitationRepository.save(invitation);

        String link = registrationBaseUrl + "?email=" + request.getEmail() + "&role=" + request.getRole().name();

        return InvitationResponse.builder()
                .email(request.getEmail())
                .role(request.getRole())
                .profileId(request.getProfileId())
                .registrationCode(code)
                .registrationLink(link)
                .expiresAt(invitation.getExpiresAt())
                .build();
    }

    @Transactional(readOnly = true)
    public boolean validateCode(ValidateCodeRequest request) {
        RegistrationInvitation invitation = invitationRepository
                .findByEmailAndCode(request.getEmail(), request.getCode())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or registration code"));

        if (invitation.isUsed()) {
            throw new IllegalArgumentException("Registration code already used");
        }
        if (invitation.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Registration code has expired");
        }
        return true;
    }

    @Transactional
    public LoginResponse setPassword(SetPasswordRequest request) {
        RegistrationInvitation invitation = invitationRepository
                .findByEmailAndCode(request.getEmail(), request.getCode())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or registration code"));

        if (invitation.isUsed()) {
            throw new IllegalArgumentException("Registration code already used");
        }
        if (invitation.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Registration code has expired");
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElse(User.builder()
                        .email(request.getEmail())
                        .role(invitation.getRole())
                        .profileId(invitation.getProfileId())
                        .enabled(true)
                        .createdAt(LocalDateTime.now())
                        .build());

        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEnabled(true);
        user.setRole(invitation.getRole());
        user.setProfileId(invitation.getProfileId());
        userRepository.save(user);

        invitation.setUsed(true);
        invitationRepository.save(invitation);

        profileActivationService.activateProfile(invitation.getRole(), invitation.getProfileId());

        return buildLoginResponse(user);
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        if (!user.isEnabled() || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        return buildLoginResponse(user);
    }

    private LoginResponse buildLoginResponse(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", user.getRole().name());
        claims.put("profileId", user.getProfileId());

        String token = JwtUtil.generateToken(jwtSecret, jwtExpirationMs, user.getEmail(), claims);

        return LoginResponse.builder()
                .token(token)
                .email(user.getEmail())
                .role(user.getRole().name())
                .profileId(user.getProfileId())
                .build();
    }

    private String generateCode() {
        SecureRandom random = new SecureRandom();
        int length = 6 + random.nextInt(2);
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < length; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }
}
