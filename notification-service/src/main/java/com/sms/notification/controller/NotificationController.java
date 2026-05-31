package com.sms.notification.controller;

import com.sms.common.dto.ApiResponse;
import com.sms.common.dto.NotificationRequest;
import com.sms.common.dto.NotificationResponse;
import com.sms.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Notification management APIs")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> findAll(
            @RequestParam(value = "role", required = false) String role) {
        return ResponseEntity.ok(ApiResponse.ok(notificationService.findAll(role)));
    }

    @GetMapping("/latest")
    public ResponseEntity<ApiResponse<NotificationResponse>> latest(
            @RequestParam(value = "role", defaultValue = "STUDENT") String role) {
        return ResponseEntity.ok(ApiResponse.ok(notificationService.latest(role)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<NotificationResponse>> create(@Valid @RequestBody NotificationRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(notificationService.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<NotificationResponse>> update(
            @PathVariable Long id, @Valid @RequestBody NotificationRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(notificationService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        notificationService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Deleted", null));
    }
}
