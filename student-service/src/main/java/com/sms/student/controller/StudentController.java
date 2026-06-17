package com.sms.student.controller;

import com.sms.common.dto.*;
import com.sms.student.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Students", description = "Student profile and dashboard APIs")
public class StudentController {

    private final StudentService studentService;

    @GetMapping("/api/students/dashboard")
    @Operation(summary = "Student dashboard with courses, progress, and latest notification")
    public ResponseEntity<ApiResponse<StudentDashboardResponse>> dashboard(
            @RequestHeader("X-Profile-Id") Long profileId) {
        studentService.seedProgressIfEmpty(profileId);
        return ResponseEntity.ok(ApiResponse.ok(studentService.dashboard(profileId)));
    }

    @GetMapping("/api/students/me")
    public ResponseEntity<ApiResponse<StudentResponse>> me(@RequestHeader("X-Profile-Id") Long profileId) {
        return ResponseEntity.ok(ApiResponse.ok(studentService.findById(profileId)));
    }

    @GetMapping("/api/students/courses")
    @Operation(summary = "Enrolled courses with assignments and exams for the current student")
    public ResponseEntity<ApiResponse<List<StudentEnrolledCourseResponse>>> enrolledCourses(
            @RequestHeader("X-Profile-Id") Long profileId) {
        studentService.seedProgressIfEmpty(profileId);
        return ResponseEntity.ok(ApiResponse.ok(studentService.getEnrolledCourses(profileId)));
    }

    @GetMapping("/api/students/courses/{courseId}/assignments")
    @Operation(summary = "Assignments for an enrolled course")
    public ResponseEntity<ApiResponse<List<AssignmentResponse>>> courseAssignments(
            @RequestHeader("X-Profile-Id") Long profileId,
            @PathVariable Long courseId) {
        return ResponseEntity.ok(ApiResponse.ok(studentService.getCourseAssignments(profileId, courseId)));
    }

    @GetMapping("/api/students/courses/{courseId}/exams")
    @Operation(summary = "Exams for an enrolled course")
    public ResponseEntity<ApiResponse<List<ExamResponse>>> courseExams(
            @RequestHeader("X-Profile-Id") Long profileId,
            @PathVariable Long courseId) {
        return ResponseEntity.ok(ApiResponse.ok(studentService.getCourseExams(profileId, courseId)));
    }

    @PostMapping("/api/students/internal")
    public ResponseEntity<ApiResponse<StudentResponse>> createInternal(@Valid @RequestBody StudentRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(studentService.create(request)));
    }

    @PutMapping("/api/students/internal/{id}")
    public ResponseEntity<ApiResponse<StudentResponse>> updateInternal(
            @PathVariable Long id, @Valid @RequestBody StudentRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(studentService.update(id, request)));
    }

    @DeleteMapping("/api/students/internal/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteInternal(@PathVariable Long id) {
        studentService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Deleted", null));
    }

    @GetMapping("/api/students/internal")
    public ResponseEntity<ApiResponse<List<StudentResponse>>> findAllInternal() {
        return ResponseEntity.ok(ApiResponse.ok(studentService.findAll()));
    }

    @PatchMapping("/api/students/internal/{id}/activate")
    public ResponseEntity<ApiResponse<StudentResponse>> activateInternal(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(studentService.activate(id)));
    }
}
