package com.sms.admin.client;

import com.sms.common.dto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "course-service", contextId = "adminAcademicClient")
public interface AcademicClient {

    @GetMapping("/api/academic/departments")
    ApiResponse<List<DepartmentResponse>> listDepartments(@RequestParam(required = false) Boolean activeOnly);

    @GetMapping("/api/academic/departments/{id}")
    ApiResponse<DepartmentResponse> getDepartment(@PathVariable("id") Long id);

    @PostMapping("/api/academic/departments")
    ApiResponse<DepartmentResponse> createDepartment(@RequestBody DepartmentRequest request);

    @PutMapping("/api/academic/departments/{id}")
    ApiResponse<DepartmentResponse> updateDepartment(@PathVariable("id") Long id, @RequestBody DepartmentRequest request);

    @DeleteMapping("/api/academic/departments/{id}")
    ApiResponse<Void> deleteDepartment(@PathVariable("id") Long id);

    @GetMapping("/api/academic/classes")
    ApiResponse<List<AcademicClassResponse>> listClasses(@RequestParam(required = false) Long departmentId);

    @GetMapping("/api/academic/classes/{id}")
    ApiResponse<AcademicClassResponse> getClass(@PathVariable("id") Long id);

    @PostMapping("/api/academic/classes")
    ApiResponse<AcademicClassResponse> createClass(@RequestBody AcademicClassRequest request);

    @PutMapping("/api/academic/classes/{id}")
    ApiResponse<AcademicClassResponse> updateClass(@PathVariable("id") Long id, @RequestBody AcademicClassRequest request);

    @DeleteMapping("/api/academic/classes/{id}")
    ApiResponse<Void> deleteClass(@PathVariable("id") Long id);

    @GetMapping("/api/academic/subjects")
    ApiResponse<List<SubjectResponse>> listSubjects(@RequestParam(required = false) Long classId);

    @GetMapping("/api/academic/subjects/{id}")
    ApiResponse<SubjectResponse> getSubject(@PathVariable("id") Long id);

    @PostMapping("/api/academic/subjects")
    ApiResponse<SubjectResponse> createSubject(@RequestBody SubjectRequest request);

    @PutMapping("/api/academic/subjects/{id}")
    ApiResponse<SubjectResponse> updateSubject(@PathVariable("id") Long id, @RequestBody SubjectRequest request);

    @DeleteMapping("/api/academic/subjects/{id}")
    ApiResponse<Void> deleteSubject(@PathVariable("id") Long id);

    @PostMapping("/api/academic/validate-selection")
    ApiResponse<AcademicSelectionResponse> validateSelection(@RequestBody AcademicSelectionRequest request);
}
