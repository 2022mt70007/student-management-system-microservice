package com.sms.course.service;

import com.sms.common.dto.AcademicSelectionRequest;
import com.sms.common.dto.AcademicSelectionResponse;
import com.sms.common.enums.AcademicStatus;
import com.sms.course.entity.AcademicClass;
import com.sms.course.entity.Department;
import com.sms.course.entity.Subject;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AcademicStructureService {

    private final DepartmentService departmentService;
    private final AcademicClassService classService;
    private final SubjectService subjectService;

    @Transactional(readOnly = true)
    public AcademicSelectionResponse validateSelection(AcademicSelectionRequest request) {
        Department department = departmentService.getEntity(request.getDepartmentId());
        if (department.getStatus() != AcademicStatus.ACTIVE) {
            throw new IllegalArgumentException("Selected department is not active");
        }

        AcademicClass academicClass = classService.getEntity(request.getClassId());
        if (!academicClass.getDepartmentId().equals(department.getId())) {
            throw new IllegalArgumentException("Selected class does not belong to the selected department");
        }

        List<Long> subjectIds = request.getSubjectIds().stream().distinct().toList();
        if (subjectIds.isEmpty()) {
            throw new IllegalArgumentException("At least one subject must be selected");
        }

        List<Subject> subjects = subjectService.findEntitiesByIds(subjectIds);
        for (Subject subject : subjects) {
            if (!subject.getClassId().equals(academicClass.getId())) {
                throw new IllegalArgumentException(
                        "Subject '" + subject.getSubjectName() + "' does not belong to the selected class");
            }
        }

        List<String> subjectNames = subjects.stream()
                .sorted(Comparator.comparing(Subject::getSubjectName, String.CASE_INSENSITIVE_ORDER))
                .map(Subject::getSubjectName)
                .toList();

        return AcademicSelectionResponse.builder()
                .departmentId(department.getId())
                .departmentName(department.getDepartmentName())
                .classId(academicClass.getId())
                .className(academicClass.getClassName())
                .subjectIds(subjectIds)
                .subjectNames(subjectNames)
                .build();
    }
}
