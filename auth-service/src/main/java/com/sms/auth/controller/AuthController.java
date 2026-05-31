package com.sms.auth.controller;

import com.sms.auth.service.AuthService;
import com.sms.common.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Login and registration APIs")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Login with email and password")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(authService.login(request)));
    }

    @PostMapping("/register/validate-code")
    @Operation(summary = "Validate admin-generated registration code")
    public ResponseEntity<ApiResponse<Boolean>> validateCode(@Valid @RequestBody ValidateCodeRequest request) {
        authService.validateCode(request);
        return ResponseEntity.ok(ApiResponse.ok("Code is valid", true));
    }

    @PostMapping("/register/set-password")
    @Operation(summary = "Complete registration by setting password")
    public ResponseEntity<ApiResponse<LoginResponse>> setPassword(@Valid @RequestBody SetPasswordRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Registration complete", authService.setPassword(request)));
    }

    @PostMapping("/invitations")
    @Operation(summary = "Create registration invitation (internal/admin)")
    public ResponseEntity<ApiResponse<InvitationResponse>> createInvitation(
            @Valid @RequestBody CreateInvitationRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(authService.createInvitation(request)));
    }
}
