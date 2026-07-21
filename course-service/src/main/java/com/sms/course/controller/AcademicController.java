package com.sms.course.controller;

import com.sms.common.dto.*;
import com.sms.course.service.AcademicClassService;
import com.sms.course.service.AcademicStructureService;
import com.sms.course.service.DepartmentService;
import com.sms.course.service.SubjectService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/academic")
@RequiredArgsConstructor
@Tag(name = "Academic Structure", description = "Department, class, and subject management")
public class AcademicController {

    private final DepartmentService departmentService;
    private final AcademicClassService classService;
    private final SubjectService subjectService;
    private final AcademicStructureService structureService;

    @GetMapping("/departments")
    public ResponseEntity<ApiResponse<List<DepartmentResponse>>> listDepartments(
            @RequestParam(required = false) Boolean activeOnly) {
        List<DepartmentResponse> data = Boolean.TRUE.equals(activeOnly)
                ? departmentService.findActive()
                : departmentService.findAll();
        return ResponseEntity.ok(ApiResponse.ok(data));
    }

    @GetMapping("/departments/{id}")
    public ResponseEntity<ApiResponse<DepartmentResponse>> getDepartment(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(departmentService.findById(id)));
    }

    @PostMapping("/departments")
    public ResponseEntity<ApiResponse<DepartmentResponse>> createDepartment(
            @Valid @RequestBody DepartmentRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(departmentService.create(request)));
    }

    @PutMapping("/departments/{id}")
    public ResponseEntity<ApiResponse<DepartmentResponse>> updateDepartment(
            @PathVariable Long id, @Valid @RequestBody DepartmentRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(departmentService.update(id, request)));
    }

    @DeleteMapping("/departments/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteDepartment(@PathVariable Long id) {
        departmentService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Deleted", null));
    }

    @GetMapping("/classes")
    public ResponseEntity<ApiResponse<List<AcademicClassResponse>>> listClasses(
            @RequestParam(required = false) Long departmentId) {
        List<AcademicClassResponse> data = departmentId != null
                ? classService.findByDepartmentId(departmentId)
                : classService.findAll();
        return ResponseEntity.ok(ApiResponse.ok(data));
    }

    @GetMapping("/classes/{id}")
    public ResponseEntity<ApiResponse<AcademicClassResponse>> getClass(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(classService.findById(id)));
    }

    @PostMapping("/classes")
    public ResponseEntity<ApiResponse<AcademicClassResponse>> createClass(
            @Valid @RequestBody AcademicClassRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(classService.create(request)));
    }

    @PutMapping("/classes/{id}")
    public ResponseEntity<ApiResponse<AcademicClassResponse>> updateClass(
            @PathVariable Long id, @Valid @RequestBody AcademicClassRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(classService.update(id, request)));
    }

    @DeleteMapping("/classes/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteClass(@PathVariable Long id) {
        classService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Deleted", null));
    }

    @GetMapping("/subjects")
    public ResponseEntity<ApiResponse<List<SubjectResponse>>> listSubjects(
            @RequestParam(required = false) Long classId) {
        List<SubjectResponse> data = classId != null
                ? subjectService.findByClassId(classId)
                : subjectService.findAll();
        return ResponseEntity.ok(ApiResponse.ok(data));
    }

    @GetMapping("/subjects/{id}")
    public ResponseEntity<ApiResponse<SubjectResponse>> getSubject(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(subjectService.findById(id)));
    }

    @PostMapping("/subjects")
    public ResponseEntity<ApiResponse<SubjectResponse>> createSubject(
            @Valid @RequestBody SubjectRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(subjectService.create(request)));
    }

    @PutMapping("/subjects/{id}")
    public ResponseEntity<ApiResponse<SubjectResponse>> updateSubject(
            @PathVariable Long id, @Valid @RequestBody SubjectRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(subjectService.update(id, request)));
    }

    @DeleteMapping("/subjects/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSubject(@PathVariable Long id) {
        subjectService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Deleted", null));
    }

    @PostMapping("/validate-selection")
    public ResponseEntity<ApiResponse<AcademicSelectionResponse>> validateSelection(
            @Valid @RequestBody AcademicSelectionRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(structureService.validateSelection(request)));
    }

    @PostMapping("/internal/subjects/by-ids")
    public ResponseEntity<ApiResponse<List<SubjectResponse>>> findSubjectsByIds(
            @RequestBody List<Long> ids) {
        return ResponseEntity.ok(ApiResponse.ok(subjectService.findByIds(ids)));
    }
}
