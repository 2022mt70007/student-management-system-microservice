package com.sms.course.service;

import com.sms.common.dto.AcademicClassRequest;
import com.sms.common.dto.AcademicClassResponse;
import com.sms.common.security.InputSanitizer;
import com.sms.course.entity.AcademicClass;
import com.sms.course.entity.Department;
import com.sms.course.repository.AcademicClassRepository;
import com.sms.course.repository.SubjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AcademicClassService {

    private final AcademicClassRepository classRepository;
    private final SubjectRepository subjectRepository;
    private final DepartmentService departmentService;

    @Transactional
    public AcademicClassResponse create(AcademicClassRequest request) {
        Department department = departmentService.getEntity(request.getDepartmentId());
        String code = InputSanitizer.cleanText(request.getClassCode()).toUpperCase();
        String name = InputSanitizer.cleanText(request.getClassName());
        if (classRepository.existsByDepartmentIdAndClassCode(department.getId(), code)) {
            throw new IllegalArgumentException("Class code already exists in this department");
        }
        if (classRepository.existsByDepartmentIdAndClassName(department.getId(), name)) {
            throw new IllegalArgumentException("Class name already exists in this department");
        }

        AcademicClass academicClass = AcademicClass.builder()
                .classCode(code)
                .className(name)
                .departmentId(department.getId())
                .description(InputSanitizer.cleanText(request.getDescription()))
                .build();
        return toResponse(classRepository.save(academicClass), department.getDepartmentName());
    }

    @Transactional
    public AcademicClassResponse update(Long id, AcademicClassRequest request) {
        AcademicClass academicClass = classRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Class not found"));
        Department department = departmentService.getEntity(request.getDepartmentId());
        String code = InputSanitizer.cleanText(request.getClassCode()).toUpperCase();
        String name = InputSanitizer.cleanText(request.getClassName());
        if (classRepository.existsByDepartmentIdAndClassCodeAndIdNot(department.getId(), code, id)) {
            throw new IllegalArgumentException("Class code already exists in this department");
        }
        if (classRepository.existsByDepartmentIdAndClassNameAndIdNot(department.getId(), name, id)) {
            throw new IllegalArgumentException("Class name already exists in this department");
        }

        academicClass.setClassCode(code);
        academicClass.setClassName(name);
        academicClass.setDepartmentId(department.getId());
        academicClass.setDescription(InputSanitizer.cleanText(request.getDescription()));
        return toResponse(classRepository.save(academicClass), department.getDepartmentName());
    }

    @Transactional
    public void delete(Long id) {
        if (!classRepository.existsById(id)) {
            throw new IllegalArgumentException("Class not found");
        }
        if (subjectRepository.countByClassId(id) > 0) {
            throw new IllegalArgumentException("Cannot delete class with existing subjects");
        }
        classRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<AcademicClassResponse> findAll() {
        Map<Long, String> departmentNames = departmentService.findAll().stream()
                .collect(Collectors.toMap(
                        com.sms.common.dto.DepartmentResponse::getId,
                        com.sms.common.dto.DepartmentResponse::getDepartmentName));
        return classRepository.findAll().stream()
                .sorted(Comparator.comparing(AcademicClass::getClassName, String.CASE_INSENSITIVE_ORDER))
                .map(c -> toResponse(c, departmentNames.get(c.getDepartmentId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AcademicClassResponse> findByDepartmentId(Long departmentId) {
        Department department = departmentService.getEntity(departmentId);
        return classRepository.findByDepartmentIdOrderByClassNameAsc(departmentId).stream()
                .map(c -> toResponse(c, department.getDepartmentName()))
                .toList();
    }

    @Transactional(readOnly = true)
    public AcademicClassResponse findById(Long id) {
        AcademicClass academicClass = classRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Class not found"));
        Department department = departmentService.getEntity(academicClass.getDepartmentId());
        return toResponse(academicClass, department.getDepartmentName());
    }

    AcademicClass getEntity(Long id) {
        return classRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Class not found"));
    }

    private AcademicClassResponse toResponse(AcademicClass academicClass, String departmentName) {
        return AcademicClassResponse.builder()
                .id(academicClass.getId())
                .classCode(academicClass.getClassCode())
                .className(academicClass.getClassName())
                .departmentId(academicClass.getDepartmentId())
                .departmentName(departmentName)
                .description(academicClass.getDescription())
                .build();
    }
}
