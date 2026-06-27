package com.sms.student.service;

import com.sms.common.dto.*;
import com.sms.common.enums.RegistrationStatus;
import com.sms.common.security.InputSanitizer;
import com.sms.student.client.AcademicClient;
import com.sms.student.client.NotificationClient;
import com.sms.student.entity.Assignment;
import com.sms.student.entity.Exam;
import com.sms.student.entity.Student;
import com.sms.student.repository.AssignmentRepository;
import com.sms.student.repository.ExamRepository;
import com.sms.student.repository.StudentProgressRepository;
import com.sms.student.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentService {

    private static final int PRIORITY_WINDOW_DAYS = 7;

    private final StudentRepository studentRepository;
    private final StudentProgressRepository progressRepository;
    private final AssignmentRepository assignmentRepository;
    private final ExamRepository examRepository;
    private final AcademicClient academicClient;
    private final NotificationClient notificationClient;

    @Transactional
    public StudentResponse create(StudentRequest request) {
        String email = InputSanitizer.normalizeEmail(request.getEmail());
        if (studentRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Student with this email already exists");
        }

        AcademicSelectionResponse selection = resolveAcademicSelection(request);

        Student student = Student.builder()
                .name(InputSanitizer.cleanText(request.getName()))
                .email(email)
                .phone(InputSanitizer.cleanText(request.getPhone()))
                .address(InputSanitizer.cleanText(request.getAddress()))
                .rollNumber(InputSanitizer.cleanText(request.getRollNumber()))
                .departmentId(selection.getDepartmentId())
                .departmentName(selection.getDepartmentName())
                .classId(selection.getClassId())
                .className(selection.getClassName())
                .subjectIds(new ArrayList<>(selection.getSubjectIds()))
                .subjectNames(new ArrayList<>(selection.getSubjectNames()))
                .status(RegistrationStatus.PENDING_REGISTRATION)
                .build();

        return toResponse(studentRepository.save(student));
    }

    @Transactional
    public StudentResponse update(Long id, StudentRequest request) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));

        AcademicSelectionResponse selection = resolveAcademicSelection(request);

        student.setName(InputSanitizer.cleanText(request.getName()));
        student.setEmail(InputSanitizer.normalizeEmail(request.getEmail()));
        student.setPhone(InputSanitizer.cleanText(request.getPhone()));
        student.setAddress(InputSanitizer.cleanText(request.getAddress()));
        student.setRollNumber(InputSanitizer.cleanText(request.getRollNumber()));
        student.setDepartmentId(selection.getDepartmentId());
        student.setDepartmentName(selection.getDepartmentName());
        student.setClassId(selection.getClassId());
        student.setClassName(selection.getClassName());
        student.setSubjectIds(new ArrayList<>(selection.getSubjectIds()));
        student.setSubjectNames(new ArrayList<>(selection.getSubjectNames()));

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

        List<SubjectResponse> subjects = getAssignedSubjects(profileId);
        NotificationResponse latest = notificationClient.latest("STUDENT").getData();

        return StudentDashboardResponse.builder()
                .className(student.getClassName())
                .departmentName(student.getDepartmentName())
                .rollNumber(student.getRollNumber())
                .subjectCount(subjects.size())
                .subjects(subjects)
                .latestNotification(latest)
                .build();
    }

    @Transactional(readOnly = true)
    public List<SubjectResponse> getAssignedSubjects(Long profileId) {
        Student student = studentRepository.findById(profileId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));
        if (student.getSubjectIds() == null || student.getSubjectIds().isEmpty()) {
            return List.of();
        }
        return academicClient.findSubjectsByIds(student.getSubjectIds()).getData();
    }

    @Transactional(readOnly = true)
    public List<AssignmentResponse> getCourseAssignments(Long profileId, Long courseId) {
        ensureEnrolled(profileId, courseId);
        return assignmentRepository.findByCourseIdOrderByDueDateAsc(courseId).stream()
                .map(this::toAssignmentResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ExamResponse> getCourseExams(Long profileId, Long courseId) {
        ensureEnrolled(profileId, courseId);
        return examRepository.findByCourseIdOrderByScheduledDateAsc(courseId).stream()
                .map(this::toExamResponse)
                .toList();
    }

    private void ensureEnrolled(Long profileId, Long courseId) {
        if (!studentRepository.existsById(profileId)) {
            throw new IllegalArgumentException("Student not found");
        }
        if (!progressRepository.existsByStudentIdAndCourseId(profileId, courseId)) {
            throw new IllegalArgumentException("You are not enrolled in this course");
        }
    }

    private AssignmentResponse toAssignmentResponse(Assignment assignment) {
        return AssignmentResponse.builder()
                .id(assignment.getId())
                .courseId(assignment.getCourseId())
                .title(assignment.getTitle())
                .description(assignment.getDescription())
                .dueDate(assignment.getDueDate())
                .priorityStatus(computeAssignmentPriority(assignment.getDueDate()))
                .build();
    }

    private ExamResponse toExamResponse(Exam exam) {
        return ExamResponse.builder()
                .id(exam.getId())
                .courseId(exam.getCourseId())
                .title(exam.getTitle())
                .description(exam.getDescription())
                .scheduledDate(exam.getScheduledDate())
                .priorityStatus(computeExamPriority(exam.getScheduledDate()))
                .build();
    }

    private String computeAssignmentPriority(LocalDate dueDate) {
        if (dueDate == null) {
            return "UPCOMING";
        }
        LocalDate today = LocalDate.now();
        if (dueDate.isBefore(today)) {
            return "OVERDUE";
        }
        if (!dueDate.isAfter(today.plusDays(PRIORITY_WINDOW_DAYS))) {
            return "DUE_SOON";
        }
        return "UPCOMING";
    }

    private String computeExamPriority(LocalDate scheduledDate) {
        if (scheduledDate == null) {
            return "UPCOMING";
        }
        LocalDate today = LocalDate.now();
        if (!scheduledDate.isBefore(today) && !scheduledDate.isAfter(today.plusDays(PRIORITY_WINDOW_DAYS))) {
            return "EXAM_SOON";
        }
        return "UPCOMING";
    }

    private AcademicSelectionResponse resolveAcademicSelection(StudentRequest request) {
        AcademicSelectionRequest selectionRequest = new AcademicSelectionRequest();
        selectionRequest.setDepartmentId(request.getDepartmentId());
        selectionRequest.setClassId(request.getClassId());
        selectionRequest.setSubjectIds(request.getSubjectIds());
        return academicClient.validateSelection(selectionRequest).getData();
    }

    private StudentResponse toResponse(Student student) {
        return StudentResponse.builder()
                .id(student.getId())
                .name(student.getName())
                .email(student.getEmail())
                .phone(student.getPhone())
                .address(student.getAddress())
                .rollNumber(student.getRollNumber())
                .departmentId(student.getDepartmentId())
                .departmentName(student.getDepartmentName())
                .classId(student.getClassId())
                .className(student.getClassName())
                .subjectIds(student.getSubjectIds())
                .subjectNames(student.getSubjectNames())
                .status(student.getStatus())
                .build();
    }
}
