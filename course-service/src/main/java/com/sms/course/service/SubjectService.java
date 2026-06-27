package com.sms.course.service;

import com.sms.common.dto.SubjectRequest;
import com.sms.common.dto.SubjectResponse;
import com.sms.common.security.InputSanitizer;
import com.sms.course.entity.AcademicClass;
import com.sms.course.entity.Department;
import com.sms.course.entity.Subject;
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
public class SubjectService {

    private final SubjectRepository subjectRepository;
    private final AcademicClassService classService;
    private final DepartmentService departmentService;

    @Transactional
    public SubjectResponse create(SubjectRequest request) {
        AcademicClass academicClass = classService.getEntity(request.getClassId());
        Department department = departmentService.getEntity(academicClass.getDepartmentId());
        String code = InputSanitizer.cleanText(request.getSubjectCode()).toUpperCase();
        String name = InputSanitizer.cleanText(request.getSubjectName());
        if (subjectRepository.existsByClassIdAndSubjectCode(academicClass.getId(), code)) {
            throw new IllegalArgumentException("Subject code already exists in this class");
        }
        if (subjectRepository.existsByClassIdAndSubjectName(academicClass.getId(), name)) {
            throw new IllegalArgumentException("Subject name already exists in this class");
        }

        Subject subject = Subject.builder()
                .subjectCode(code)
                .subjectName(name)
                .classId(academicClass.getId())
                .credits(request.getCredits())
                .description(InputSanitizer.cleanText(request.getDescription()))
                .build();
        subject = subjectRepository.save(subject);
        return toResponse(subject, academicClass, department.getDepartmentName());
    }

    @Transactional
    public SubjectResponse update(Long id, SubjectRequest request) {
        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Subject not found"));
        AcademicClass academicClass = classService.getEntity(request.getClassId());
        Department department = departmentService.getEntity(academicClass.getDepartmentId());
        String code = InputSanitizer.cleanText(request.getSubjectCode()).toUpperCase();
        String name = InputSanitizer.cleanText(request.getSubjectName());
        if (subjectRepository.existsByClassIdAndSubjectCodeAndIdNot(academicClass.getId(), code, id)) {
            throw new IllegalArgumentException("Subject code already exists in this class");
        }
        if (subjectRepository.existsByClassIdAndSubjectNameAndIdNot(academicClass.getId(), name, id)) {
            throw new IllegalArgumentException("Subject name already exists in this class");
        }

        subject.setSubjectCode(code);
        subject.setSubjectName(name);
        subject.setClassId(academicClass.getId());
        subject.setCredits(request.getCredits());
        subject.setDescription(InputSanitizer.cleanText(request.getDescription()));
        return toResponse(subjectRepository.save(subject), academicClass, department.getDepartmentName());
    }

    @Transactional
    public void delete(Long id) {
        if (!subjectRepository.existsById(id)) {
            throw new IllegalArgumentException("Subject not found");
        }
        subjectRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<SubjectResponse> findAll() {
        Map<Long, AcademicClass> classesById = classService.findAll().stream()
                .collect(Collectors.toMap(
                        com.sms.common.dto.AcademicClassResponse::getId,
                        r -> classService.getEntity(r.getId())));
        Map<Long, String> departmentNames = departmentService.findAll().stream()
                .collect(Collectors.toMap(
                        com.sms.common.dto.DepartmentResponse::getId,
                        com.sms.common.dto.DepartmentResponse::getDepartmentName));

        return subjectRepository.findAll().stream()
                .sorted(Comparator.comparing(Subject::getSubjectName, String.CASE_INSENSITIVE_ORDER))
                .map(s -> {
                    AcademicClass cls = classesById.get(s.getClassId());
                    String deptName = cls != null ? departmentNames.get(cls.getDepartmentId()) : null;
                    return toResponse(s, cls, deptName);
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SubjectResponse> findByClassId(Long classId) {
        AcademicClass academicClass = classService.getEntity(classId);
        Department department = departmentService.getEntity(academicClass.getDepartmentId());
        return subjectRepository.findByClassIdOrderBySubjectNameAsc(classId).stream()
                .map(s -> toResponse(s, academicClass, department.getDepartmentName()))
                .toList();
    }

    @Transactional(readOnly = true)
    public SubjectResponse findById(Long id) {
        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Subject not found"));
        AcademicClass academicClass = classService.getEntity(subject.getClassId());
        Department department = departmentService.getEntity(academicClass.getDepartmentId());
        return toResponse(subject, academicClass, department.getDepartmentName());
    }

    @Transactional(readOnly = true)
    public List<SubjectResponse> findByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        List<Long> distinctIds = ids.stream().distinct().toList();
        Map<Long, AcademicClass> classesById = classService.findAll().stream()
                .collect(Collectors.toMap(
                        com.sms.common.dto.AcademicClassResponse::getId,
                        r -> classService.getEntity(r.getId())));
        Map<Long, String> departmentNames = departmentService.findAll().stream()
                .collect(Collectors.toMap(
                        com.sms.common.dto.DepartmentResponse::getId,
                        com.sms.common.dto.DepartmentResponse::getDepartmentName));

        return subjectRepository.findAllById(distinctIds).stream()
                .sorted(Comparator.comparing(Subject::getSubjectName, String.CASE_INSENSITIVE_ORDER))
                .map(subject -> {
                    AcademicClass cls = classesById.get(subject.getClassId());
                    String deptName = cls != null ? departmentNames.get(cls.getDepartmentId()) : null;
                    return toResponse(subject, cls, deptName);
                })
                .toList();
    }

    List<Subject> findEntitiesByIds(List<Long> ids) {
        List<Subject> subjects = subjectRepository.findAllById(ids);
        if (subjects.size() != ids.size()) {
            throw new IllegalArgumentException("One or more subjects were not found");
        }
        return subjects;
    }

    private SubjectResponse toResponse(Subject subject, AcademicClass academicClass, String departmentName) {
        return SubjectResponse.builder()
                .id(subject.getId())
                .subjectCode(subject.getSubjectCode())
                .subjectName(subject.getSubjectName())
                .classId(subject.getClassId())
                .className(academicClass != null ? academicClass.getClassName() : null)
                .departmentId(academicClass != null ? academicClass.getDepartmentId() : null)
                .departmentName(departmentName)
                .credits(subject.getCredits())
                .description(subject.getDescription())
                .build();
    }
}
