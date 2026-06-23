package com.sms.admin.service;

import com.sms.admin.client.AcademicClient;
import com.sms.common.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AcademicManagementService {

    private final AcademicClient academicClient;

    public List<DepartmentResponse> listDepartments(Boolean activeOnly) {
        return academicClient.listDepartments(activeOnly).getData();
    }

    public DepartmentResponse createDepartment(DepartmentRequest request) {
        return academicClient.createDepartment(request).getData();
    }

    public DepartmentResponse updateDepartment(Long id, DepartmentRequest request) {
        return academicClient.updateDepartment(id, request).getData();
    }

    public void deleteDepartment(Long id) {
        academicClient.deleteDepartment(id);
    }

    public List<AcademicClassResponse> listClasses(Long departmentId) {
        return academicClient.listClasses(departmentId).getData();
    }

    public AcademicClassResponse createClass(AcademicClassRequest request) {
        return academicClient.createClass(request).getData();
    }

    public AcademicClassResponse updateClass(Long id, AcademicClassRequest request) {
        return academicClient.updateClass(id, request).getData();
    }

    public void deleteClass(Long id) {
        academicClient.deleteClass(id);
    }

    public List<SubjectResponse> listSubjects(Long classId) {
        return academicClient.listSubjects(classId).getData();
    }

    public SubjectResponse createSubject(SubjectRequest request) {
        return academicClient.createSubject(request).getData();
    }

    public SubjectResponse updateSubject(Long id, SubjectRequest request) {
        return academicClient.updateSubject(id, request).getData();
    }

    public void deleteSubject(Long id) {
        academicClient.deleteSubject(id);
    }

    public AcademicSelectionResponse validateSelection(AcademicSelectionRequest request) {
        return academicClient.validateSelection(request).getData();
    }
}
