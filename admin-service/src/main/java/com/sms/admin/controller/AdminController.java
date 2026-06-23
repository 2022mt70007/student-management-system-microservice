package com.sms.admin.controller;

import com.sms.admin.service.AcademicManagementService;
import com.sms.admin.service.AdminOrchestrationService;
import com.sms.common.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "Admin dashboard and user management")
public class AdminController {

    private final AdminOrchestrationService adminService;
    private final AcademicManagementService academicService;

    @GetMapping("/dashboard")
    @Operation(summary = "Admin dashboard with all tabs data")
    public ResponseEntity<ApiResponse<Map<String, Object>>> dashboard() {
        return ResponseEntity.ok(ApiResponse.ok(adminService.dashboard()));
    }

    @GetMapping("/students")
    public ResponseEntity<ApiResponse<List<StudentResponse>>> listStudents() {
        return ResponseEntity.ok(ApiResponse.ok(adminService.listStudents()));
    }

    @PostMapping("/students")
    public ResponseEntity<ApiResponse<StudentResponse>> createStudent(@Valid @RequestBody StudentRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Student created. Registration email sent.", adminService.createStudent(request)));
    }

    @PutMapping("/students/{id}")
    public ResponseEntity<ApiResponse<StudentResponse>> updateStudent(
            @PathVariable Long id, @Valid @RequestBody StudentRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(adminService.updateStudent(id, request)));
    }

    @DeleteMapping("/students/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteStudent(@PathVariable Long id) {
        adminService.deleteStudent(id);
        return ResponseEntity.ok(ApiResponse.ok("Student deleted", null));
    }

    @GetMapping("/teachers")
    public ResponseEntity<ApiResponse<List<TeacherResponse>>> listTeachers() {
        return ResponseEntity.ok(ApiResponse.ok(adminService.listTeachers()));
    }

    @PostMapping("/teachers")
    public ResponseEntity<ApiResponse<TeacherResponse>> createTeacher(@Valid @RequestBody TeacherRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Teacher created. Registration email sent.", adminService.createTeacher(request)));
    }

    @PutMapping("/teachers/{id}")
    public ResponseEntity<ApiResponse<TeacherResponse>> updateTeacher(
            @PathVariable Long id, @Valid @RequestBody TeacherRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(adminService.updateTeacher(id, request)));
    }

    @DeleteMapping("/teachers/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTeacher(@PathVariable Long id) {
        adminService.deleteTeacher(id);
        return ResponseEntity.ok(ApiResponse.ok("Teacher deleted", null));
    }

    @GetMapping("/admins")
    public ResponseEntity<ApiResponse<List<AdminUserResponse>>> listAdmins() {
        return ResponseEntity.ok(ApiResponse.ok(adminService.listAdmins()));
    }

    @PostMapping("/admins")
    public ResponseEntity<ApiResponse<AdminUserResponse>> createAdmin(@Valid @RequestBody AdminUserRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Admin created. Registration email sent.", adminService.createAdmin(request)));
    }

    @PutMapping("/admins/{id}")
    public ResponseEntity<ApiResponse<AdminUserResponse>> updateAdmin(
            @PathVariable Long id, @Valid @RequestBody AdminUserRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(adminService.updateAdmin(id, request)));
    }

    @DeleteMapping("/admins/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAdmin(@PathVariable Long id) {
        adminService.deleteAdmin(id);
        return ResponseEntity.ok(ApiResponse.ok("Admin deleted", null));
    }

    @GetMapping("/courses")
    public ResponseEntity<ApiResponse<List<CourseResponse>>> listCourses() {
        return ResponseEntity.ok(ApiResponse.ok(adminService.listCourses()));
    }

    @PostMapping("/courses")
    public ResponseEntity<ApiResponse<CourseResponse>> createCourse(@Valid @RequestBody CourseRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(adminService.createCourse(request)));
    }

    @PutMapping("/courses/{id}")
    public ResponseEntity<ApiResponse<CourseResponse>> updateCourse(
            @PathVariable Long id, @Valid @RequestBody CourseRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(adminService.updateCourse(id, request)));
    }

    @DeleteMapping("/courses/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCourse(@PathVariable Long id) {
        adminService.deleteCourse(id);
        return ResponseEntity.ok(ApiResponse.ok("Course deleted", null));
    }

    @GetMapping("/notifications")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> listNotifications() {
        return ResponseEntity.ok(ApiResponse.ok(adminService.listNotifications()));
    }

    @PostMapping("/notifications")
    public ResponseEntity<ApiResponse<NotificationResponse>> createNotification(
            @Valid @RequestBody NotificationRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(adminService.createNotification(request)));
    }

    @PutMapping("/notifications/{id}")
    public ResponseEntity<ApiResponse<NotificationResponse>> updateNotification(
            @PathVariable Long id, @Valid @RequestBody NotificationRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(adminService.updateNotification(id, request)));
    }

    @DeleteMapping("/notifications/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteNotification(@PathVariable Long id) {
        adminService.deleteNotification(id);
        return ResponseEntity.ok(ApiResponse.ok("Notification deleted", null));
    }

    @PatchMapping("/internal/{id}/activate")
    public ResponseEntity<ApiResponse<Void>> activateAdmin(@PathVariable Long id) {
        adminService.activateAdmin(id);
        return ResponseEntity.ok(ApiResponse.ok("Admin activated", null));
    }

    @GetMapping("/academic/departments")
    public ResponseEntity<ApiResponse<List<DepartmentResponse>>> listDepartments(
            @RequestParam(required = false) Boolean activeOnly) {
        return ResponseEntity.ok(ApiResponse.ok(academicService.listDepartments(activeOnly)));
    }

    @PostMapping("/academic/departments")
    public ResponseEntity<ApiResponse<DepartmentResponse>> createDepartment(
            @Valid @RequestBody DepartmentRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(academicService.createDepartment(request)));
    }

    @PutMapping("/academic/departments/{id}")
    public ResponseEntity<ApiResponse<DepartmentResponse>> updateDepartment(
            @PathVariable Long id, @Valid @RequestBody DepartmentRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(academicService.updateDepartment(id, request)));
    }

    @DeleteMapping("/academic/departments/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteDepartment(@PathVariable Long id) {
        academicService.deleteDepartment(id);
        return ResponseEntity.ok(ApiResponse.ok("Department deleted", null));
    }

    @GetMapping("/academic/classes")
    public ResponseEntity<ApiResponse<List<AcademicClassResponse>>> listClasses(
            @RequestParam(required = false) Long departmentId) {
        return ResponseEntity.ok(ApiResponse.ok(academicService.listClasses(departmentId)));
    }

    @PostMapping("/academic/classes")
    public ResponseEntity<ApiResponse<AcademicClassResponse>> createClass(
            @Valid @RequestBody AcademicClassRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(academicService.createClass(request)));
    }

    @PutMapping("/academic/classes/{id}")
    public ResponseEntity<ApiResponse<AcademicClassResponse>> updateClass(
            @PathVariable Long id, @Valid @RequestBody AcademicClassRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(academicService.updateClass(id, request)));
    }

    @DeleteMapping("/academic/classes/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteClass(@PathVariable Long id) {
        academicService.deleteClass(id);
        return ResponseEntity.ok(ApiResponse.ok("Class deleted", null));
    }

    @GetMapping("/academic/subjects")
    public ResponseEntity<ApiResponse<List<SubjectResponse>>> listSubjects(
            @RequestParam(required = false) Long classId) {
        return ResponseEntity.ok(ApiResponse.ok(academicService.listSubjects(classId)));
    }

    @PostMapping("/academic/subjects")
    public ResponseEntity<ApiResponse<SubjectResponse>> createSubject(
            @Valid @RequestBody SubjectRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(academicService.createSubject(request)));
    }

    @PutMapping("/academic/subjects/{id}")
    public ResponseEntity<ApiResponse<SubjectResponse>> updateSubject(
            @PathVariable Long id, @Valid @RequestBody SubjectRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(academicService.updateSubject(id, request)));
    }

    @DeleteMapping("/academic/subjects/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSubject(@PathVariable Long id) {
        academicService.deleteSubject(id);
        return ResponseEntity.ok(ApiResponse.ok("Subject deleted", null));
    }

    @PostMapping("/academic/validate-selection")
    public ResponseEntity<ApiResponse<AcademicSelectionResponse>> validateSelection(
            @Valid @RequestBody AcademicSelectionRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(academicService.validateSelection(request)));
    }
}
