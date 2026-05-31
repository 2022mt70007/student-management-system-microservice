package com.sms.teacher.service;

import com.sms.common.dto.*;
import com.sms.common.enums.RegistrationStatus;
import com.sms.teacher.client.CourseClient;
import com.sms.teacher.client.NotificationClient;
import com.sms.teacher.client.StudentClient;
import com.sms.teacher.entity.Teacher;
import com.sms.teacher.repository.TeacherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TeacherService {

    private final TeacherRepository teacherRepository;
    private final CourseClient courseClient;
    private final NotificationClient notificationClient;
    private final StudentClient studentClient;

    @Transactional
    public TeacherResponse create(TeacherRequest request) {
        if (teacherRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Teacher with this email already exists");
        }

        Teacher teacher = Teacher.builder()
                .name(request.getName())
                .teacherId(request.getTeacherId())
                .department(request.getDepartment())
                .email(request.getEmail())
                .phone(request.getPhone())
                .address(request.getAddress())
                .subjects(request.getSubjects() != null ? request.getSubjects() : List.of())
                .status(RegistrationStatus.PENDING_REGISTRATION)
                .build();

        return toResponse(teacherRepository.save(teacher));
    }

    @Transactional
    public TeacherResponse update(Long id, TeacherRequest request) {
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Teacher not found"));

        teacher.setName(request.getName());
        teacher.setTeacherId(request.getTeacherId());
        teacher.setDepartment(request.getDepartment());
        teacher.setEmail(request.getEmail());
        teacher.setPhone(request.getPhone());
        teacher.setAddress(request.getAddress());
        teacher.setSubjects(request.getSubjects() != null ? request.getSubjects() : List.of());

        return toResponse(teacherRepository.save(teacher));
    }

    @Transactional
    public void delete(Long id) {
        if (!teacherRepository.existsById(id)) {
            throw new IllegalArgumentException("Teacher not found");
        }
        teacherRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<TeacherResponse> findAll() {
        return teacherRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public TeacherResponse findById(Long id) {
        return toResponse(teacherRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Teacher not found")));
    }

    @Transactional
    public TeacherResponse activate(Long id) {
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Teacher not found"));
        teacher.setStatus(RegistrationStatus.ACTIVE);
        return toResponse(teacherRepository.save(teacher));
    }

    @Transactional(readOnly = true)
    public TeacherDashboardResponse dashboard() {
        return TeacherDashboardResponse.builder()
                .courses(courseClient.findAll().getData())
                .notifications(notificationClient.findAll("TEACHER").getData())
                .students(studentClient.findAll().getData())
                .build();
    }

    private TeacherResponse toResponse(Teacher teacher) {
        return TeacherResponse.builder()
                .id(teacher.getId())
                .name(teacher.getName())
                .teacherId(teacher.getTeacherId())
                .department(teacher.getDepartment())
                .email(teacher.getEmail())
                .phone(teacher.getPhone())
                .address(teacher.getAddress())
                .subjects(teacher.getSubjects())
                .status(teacher.getStatus())
                .build();
    }
}
