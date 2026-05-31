package com.sms.student.service;

import com.sms.common.dto.*;
import com.sms.common.enums.RegistrationStatus;
import com.sms.student.client.CourseClient;
import com.sms.student.client.NotificationClient;
import com.sms.student.entity.Student;
import com.sms.student.entity.StudentProgress;
import com.sms.student.repository.StudentProgressRepository;
import com.sms.student.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;
    private final StudentProgressRepository progressRepository;
    private final CourseClient courseClient;
    private final NotificationClient notificationClient;

    @Transactional
    public StudentResponse create(StudentRequest request) {
        if (studentRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Student with this email already exists");
        }

        Student student = Student.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .address(request.getAddress())
                .rollNumber(request.getRollNumber())
                .className(request.getClassName())
                .department(request.getDepartment())
                .subjects(request.getSubjects() != null ? request.getSubjects() : List.of())
                .status(RegistrationStatus.PENDING_REGISTRATION)
                .build();

        return toResponse(studentRepository.save(student));
    }

    @Transactional
    public StudentResponse update(Long id, StudentRequest request) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));

        student.setName(request.getName());
        student.setEmail(request.getEmail());
        student.setPhone(request.getPhone());
        student.setAddress(request.getAddress());
        student.setRollNumber(request.getRollNumber());
        student.setClassName(request.getClassName());
        student.setDepartment(request.getDepartment());
        student.setSubjects(request.getSubjects() != null ? request.getSubjects() : List.of());

        return toResponse(studentRepository.save(student));
    }

    @Transactional
    public void delete(Long id) {
        if (!studentRepository.existsById(id)) {
            throw new IllegalArgumentException("Student not found");
        }
        studentRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<StudentResponse> findAll() {
        return studentRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public StudentResponse findById(Long id) {
        return toResponse(studentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Student not found")));
    }

    @Transactional
    public StudentResponse activate(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));
        student.setStatus(RegistrationStatus.ACTIVE);
        return toResponse(studentRepository.save(student));
    }

    @Transactional(readOnly = true)
    public StudentDashboardResponse dashboard(Long profileId) {
        Student student = studentRepository.findById(profileId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));

        List<CourseResponse> courses = courseClient.findAll().getData();
        List<StudentProgressResponse> progress = progressRepository.findByStudentId(profileId).stream()
                .map(p -> StudentProgressResponse.builder()
                        .courseId(p.getCourseId())
                        .courseTitle(p.getCourseTitle())
                        .progressPercent(p.getProgressPercent())
                        .build())
                .toList();

        NotificationResponse latest = notificationClient.latest("STUDENT").getData();

        return StudentDashboardResponse.builder()
                .courses(courses)
                .progress(progress)
                .latestNotification(latest)
                .build();
    }

    @Transactional
    public void seedProgressIfEmpty(Long studentId) {
        if (progressRepository.findByStudentId(studentId).isEmpty()) {
            List<CourseResponse> courses = courseClient.findAll().getData();
            for (CourseResponse course : courses) {
                StudentProgress progress = StudentProgress.builder()
                        .studentId(studentId)
                        .courseId(course.getId())
                        .courseTitle(course.getTitle())
                        .progressPercent(0)
                        .build();
                progressRepository.save(progress);
            }
        }
    }

    private StudentResponse toResponse(Student student) {
        return StudentResponse.builder()
                .id(student.getId())
                .name(student.getName())
                .email(student.getEmail())
                .phone(student.getPhone())
                .address(student.getAddress())
                .rollNumber(student.getRollNumber())
                .className(student.getClassName())
                .department(student.getDepartment())
                .subjects(student.getSubjects())
                .status(student.getStatus())
                .build();
    }
}
