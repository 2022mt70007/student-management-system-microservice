package com.sms.course.service;

import com.sms.common.dto.*;
import com.sms.common.enums.AcademicStatus;
import com.sms.common.security.InputSanitizer;
import com.sms.course.entity.AcademicClass;
import com.sms.course.entity.Department;
import com.sms.course.entity.Subject;
import com.sms.course.repository.AcademicClassRepository;
import com.sms.course.repository.AcademicClassRepository;
import com.sms.course.repository.DepartmentRepository;
import com.sms.course.repository.SubjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final AcademicClassRepository classRepository;

    @Transactional
    public DepartmentResponse create(DepartmentRequest request) {
        String code = InputSanitizer.cleanText(request.getDepartmentCode()).toUpperCase();
        String name = InputSanitizer.cleanText(request.getDepartmentName());
        if (departmentRepository.existsByDepartmentCode(code)) {
            throw new IllegalArgumentException("Department code already exists");
        }
        if (departmentRepository.existsByDepartmentName(name)) {
            throw new IllegalArgumentException("Department name already exists");
        }

        Department department = Department.builder()
                .departmentCode(code)
                .departmentName(name)
                .description(InputSanitizer.cleanText(request.getDescription()))
                .status(request.getStatus() != null ? request.getStatus() : AcademicStatus.ACTIVE)
                .build();
        return toResponse(departmentRepository.save(department));
    }

    @Transactional
    public DepartmentResponse update(Long id, DepartmentRequest request) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Department not found"));
        String code = InputSanitizer.cleanText(request.getDepartmentCode()).toUpperCase();
        String name = InputSanitizer.cleanText(request.getDepartmentName());
        if (departmentRepository.existsByDepartmentCodeAndIdNot(code, id)) {
            throw new IllegalArgumentException("Department code already exists");
        }
        if (departmentRepository.existsByDepartmentNameAndIdNot(name, id)) {
            throw new IllegalArgumentException("Department name already exists");
        }

        department.setDepartmentCode(code);
        department.setDepartmentName(name);
        department.setDescription(InputSanitizer.cleanText(request.getDescription()));
        department.setStatus(request.getStatus() != null ? request.getStatus() : department.getStatus());
        return toResponse(departmentRepository.save(department));
    }

    @Transactional
    public void delete(Long id) {
        if (!departmentRepository.existsById(id)) {
            throw new IllegalArgumentException("Department not found");
        }
        if (classRepository.countByDepartmentId(id) > 0) {
            throw new IllegalArgumentException("Cannot delete department with existing classes");
        }
        departmentRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<DepartmentResponse> findAll() {
        return departmentRepository.findAll().stream()
                .sorted(Comparator.comparing(Department::getDepartmentName, String.CASE_INSENSITIVE_ORDER))
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DepartmentResponse> findActive() {
        return departmentRepository.findByStatusOrderByDepartmentNameAsc(AcademicStatus.ACTIVE).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public DepartmentResponse findById(Long id) {
        return toResponse(departmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Department not found")));
    }

    Department getEntity(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Department not found"));
    }

    private DepartmentResponse toResponse(Department department) {
        return DepartmentResponse.builder()
                .id(department.getId())
                .departmentCode(department.getDepartmentCode())
                .departmentName(department.getDepartmentName())
                .description(department.getDescription())
                .status(department.getStatus())
                .build();
    }
}
