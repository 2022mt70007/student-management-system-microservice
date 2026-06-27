package com.sms.teacher.controller;

import com.sms.common.dto.*;
import com.sms.teacher.service.TeacherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Teachers", description = "Teacher profile and dashboard APIs")
public class TeacherController {

    private final TeacherService teacherService;

    @GetMapping("/api/teachers/dashboard")
    @Operation(summary = "Teacher dashboard with courses, notifications, and students")
    public ResponseEntity<ApiResponse<TeacherDashboardResponse>> dashboard() {
        return ResponseEntity.ok(ApiResponse.ok(teacherService.dashboard()));
    }

    @GetMapping("/api/teachers/me")
    public ResponseEntity<ApiResponse<TeacherResponse>> me(@RequestHeader("X-Profile-Id") Long profileId) {
        return ResponseEntity.ok(ApiResponse.ok(teacherService.findById(profileId)));
    }

    @PostMapping("/api/teachers/internal")
    public ResponseEntity<ApiResponse<TeacherResponse>> createInternal(@Valid @RequestBody TeacherRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(teacherService.create(request)));
    }

    @PutMapping("/api/teachers/internal/{id}")
    public ResponseEntity<ApiResponse<TeacherResponse>> updateInternal(
            @PathVariable Long id, @Valid @RequestBody TeacherRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(teacherService.update(id, request)));
    }

    @DeleteMapping("/api/teachers/internal/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteInternal(@PathVariable Long id) {
        teacherService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Deleted", null));
    }

    @GetMapping("/api/teachers/internal")
    public ResponseEntity<ApiResponse<List<TeacherResponse>>> findAllInternal() {
        return ResponseEntity.ok(ApiResponse.ok(teacherService.findAll()));
    }

    @PostMapping("/api/teachers/internal/{id}/activate")
    public ResponseEntity<ApiResponse<TeacherResponse>> activateInternal(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(teacherService.activate(id)));
    }
}
