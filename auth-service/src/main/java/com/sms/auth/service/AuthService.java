package com.sms.auth.service;

import com.sms.auth.entity.RegistrationInvitation;
import com.sms.auth.entity.User;
import com.sms.auth.repository.RegistrationInvitationRepository;
import com.sms.auth.repository.UserRepository;
import com.sms.common.dto.*;
import com.sms.common.security.InputSanitizer;
import com.sms.common.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
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

    @Value("${app.security.max-failed-login-attempts:5}")
    private int maxFailedLoginAttempts;

    @Value("${app.security.account-lock-minutes:15}")
    private int accountLockMinutes;

    @Value("${app.security.max-verification-attempts:8}")
    private int maxVerificationAttempts;

    @Value("${app.security.verification-lock-minutes:30}")
    private int verificationLockMinutes;

    private final Map<String, AttemptState> verificationAttempts = new ConcurrentHashMap<>();

    @Transactional
    public InvitationResponse createInvitation(CreateInvitationRequest request) {
        String email = InputSanitizer.normalizeEmail(request.getEmail());
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("User already registered with this email");
        }

        String registrationCode = InputSanitizer.cleanText(request.getRegistrationCode());
        String code = registrationCode != null && !registrationCode.isBlank()
                ? registrationCode
                : generateCode();

        invitationRepository.findByEmail(email).ifPresent(invitationRepository::delete);

        RegistrationInvitation invitation = RegistrationInvitation.builder()
                .email(email)
                .code(code)
                .role(request.getRole())
                .profileId(request.getProfileId())
                .used(false)
                .expiresAt(LocalDateTime.now().plusDays(codeExpiryDays))
                .createdAt(LocalDateTime.now())
                .build();

        invitationRepository.save(invitation);

        String link = registrationBaseUrl + "?email=" + email + "&role=" + request.getRole().name();

        return InvitationResponse.builder()
                .email(email)
                .role(request.getRole())
                .profileId(request.getProfileId())
                .registrationCode(code)
                .registrationLink(link)
                .expiresAt(invitation.getExpiresAt())
                .build();
    }

    @Transactional(readOnly = true)
    public boolean validateCode(ValidateCodeRequest request) {
        String email = InputSanitizer.normalizeEmail(request.getEmail());
        String code = InputSanitizer.cleanText(request.getCode());
        assertVerificationAllowed(email);
        try {
            RegistrationInvitation invitation = invitationRepository
                    .findByEmailAndCode(email, code)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid email or registration code"));

            if (invitation.isUsed()) {
                throw new IllegalArgumentException("Registration code already used");
            }
            if (invitation.getExpiresAt().isBefore(LocalDateTime.now())) {
                throw new IllegalArgumentException("Registration code has expired");
            }
            clearVerificationAttempts(email);
            return true;
        } catch (IllegalArgumentException ex) {
            recordVerificationFailure(email);
            throw ex;
        }
    }

    @Transactional
    public LoginResponse setPassword(SetPasswordRequest request) {
        String email = InputSanitizer.normalizeEmail(request.getEmail());
        String code = InputSanitizer.cleanText(request.getCode());
        assertVerificationAllowed(email);
        try {
            RegistrationInvitation invitation = invitationRepository
                    .findByEmailAndCode(email, code)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid email or registration code"));

            if (invitation.isUsed()) {
                throw new IllegalArgumentException("Registration code already used");
            }
            if (invitation.getExpiresAt().isBefore(LocalDateTime.now())) {
                throw new IllegalArgumentException("Registration code has expired");
            }

            User user = userRepository.findByEmail(email)
                    .orElse(User.builder()
                            .email(email)
                            .role(invitation.getRole())
                            .profileId(invitation.getProfileId())
                            .enabled(true)
                            .createdAt(LocalDateTime.now())
                            .build());

            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setEnabled(true);
            user.setRole(invitation.getRole());
            user.setProfileId(invitation.getProfileId());
            resetLoginLock(user);
            userRepository.save(user);

            invitation.setUsed(true);
            invitationRepository.save(invitation);
            clearVerificationAttempts(email);

            profileActivationService.activateProfile(invitation.getRole(), invitation.getProfileId());

            return buildLoginResponse(user);
        } catch (IllegalArgumentException ex) {
            recordVerificationFailure(email);
            throw ex;
        }
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        String email = InputSanitizer.normalizeEmail(request.getEmail());
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        if (isAccountLocked(user)) {
            throw new IllegalArgumentException("Account temporarily locked. Try again later.");
        }

        if (!user.isEnabled() || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            registerFailedLogin(user);
            throw new IllegalArgumentException("Invalid credentials");
        }

        resetLoginLock(user);
        userRepository.save(user);
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

    private boolean isAccountLocked(User user) {
        return user.getAccountLockedUntil() != null && user.getAccountLockedUntil().isAfter(LocalDateTime.now());
    }

    private void registerFailedLogin(User user) {
        int attempts = user.getFailedLoginAttempts() + 1;
        user.setFailedLoginAttempts(attempts);
        if (attempts >= maxFailedLoginAttempts) {
            user.setAccountLockedUntil(LocalDateTime.now().plusMinutes(accountLockMinutes));
            user.setFailedLoginAttempts(0);
        }
        userRepository.save(user);
    }

    private void resetLoginLock(User user) {
        user.setFailedLoginAttempts(0);
        user.setAccountLockedUntil(null);
    }

    private void assertVerificationAllowed(String email) {
        AttemptState state = verificationAttempts.get(email.toLowerCase());
        if (state == null) {
            return;
        }
        if (state.lockedUntil != null && state.lockedUntil.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("Too many verification attempts. Try again later.");
        }
        if (state.lockedUntil != null && state.lockedUntil.isBefore(LocalDateTime.now())) {
            verificationAttempts.remove(email.toLowerCase());
        }
    }

    private void recordVerificationFailure(String email) {
        String key = email.toLowerCase();
        verificationAttempts.compute(key, (k, existing) -> {
            AttemptState state = existing == null ? new AttemptState() : existing;
            if (state.lockedUntil != null && state.lockedUntil.isAfter(LocalDateTime.now())) {
                return state;
            }

            state.failures++;
            if (state.failures >= maxVerificationAttempts) {
                state.failures = 0;
                state.lockedUntil = LocalDateTime.now().plusMinutes(verificationLockMinutes);
            }
            return state;
        });
    }

    private void clearVerificationAttempts(String email) {
        verificationAttempts.remove(email.toLowerCase());
    }

    private static class AttemptState {
        int failures;
        LocalDateTime lockedUntil;
    }
}
