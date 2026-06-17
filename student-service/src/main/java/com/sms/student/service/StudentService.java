package com.sms.student.service;

import com.sms.common.dto.*;
import com.sms.common.enums.RegistrationStatus;
import com.sms.common.security.InputSanitizer;
import com.sms.student.client.CourseClient;
import com.sms.student.client.NotificationClient;
import com.sms.student.entity.Assignment;
import com.sms.student.entity.Exam;
import com.sms.student.entity.Student;
import com.sms.student.entity.StudentProgress;
import com.sms.student.repository.AssignmentRepository;
import com.sms.student.repository.ExamRepository;
import com.sms.student.repository.StudentProgressRepository;
import com.sms.student.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudentService {

    private static final int PRIORITY_WINDOW_DAYS = 7;

    private final StudentRepository studentRepository;
    private final StudentProgressRepository progressRepository;
    private final AssignmentRepository assignmentRepository;
    private final ExamRepository examRepository;
    private final CourseClient courseClient;
    private final NotificationClient notificationClient;

    @Transactional
    public StudentResponse create(StudentRequest request) {
        String email = InputSanitizer.normalizeEmail(request.getEmail());
        if (studentRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Student with this email already exists");
        }

        Student student = Student.builder()
                .name(InputSanitizer.cleanText(request.getName()))
                .email(email)
                .phone(InputSanitizer.cleanText(request.getPhone()))
                .address(InputSanitizer.cleanText(request.getAddress()))
                .rollNumber(InputSanitizer.cleanText(request.getRollNumber()))
                .className(InputSanitizer.cleanText(request.getClassName()))
                .department(InputSanitizer.cleanText(request.getDepartment()))
                .subjects(InputSanitizer.cleanList(request.getSubjects()))
                .status(RegistrationStatus.PENDING_REGISTRATION)
                .build();

        return toResponse(studentRepository.save(student));
    }

    @Transactional
    public StudentResponse update(Long id, StudentRequest request) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));

        student.setName(InputSanitizer.cleanText(request.getName()));
        student.setEmail(InputSanitizer.normalizeEmail(request.getEmail()));
        student.setPhone(InputSanitizer.cleanText(request.getPhone()));
        student.setAddress(InputSanitizer.cleanText(request.getAddress()));
        student.setRollNumber(InputSanitizer.cleanText(request.getRollNumber()));
        student.setClassName(InputSanitizer.cleanText(request.getClassName()));
        student.setDepartment(InputSanitizer.cleanText(request.getDepartment()));
        student.setSubjects(InputSanitizer.cleanList(request.getSubjects()));

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
        studentRepository.findById(profileId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));

        List<StudentProgressResponse> progress = progressRepository.findByStudentId(profileId).stream()
                .map(p -> StudentProgressResponse.builder()
                        .courseId(p.getCourseId())
                        .courseTitle(p.getCourseTitle())
                        .progressPercent(p.getProgressPercent())
                        .build())
                .toList();

        NotificationResponse latest = notificationClient.latest("STUDENT").getData();

        return StudentDashboardResponse.builder()
                .progress(progress)
                .latestNotification(latest)
                .build();
    }

    @Transactional(readOnly = true)
    public List<StudentEnrolledCourseResponse> getEnrolledCourses(Long profileId) {
        ensureStudentExists(profileId);
        Map<Long, StudentProgress> progressByCourse = progressRepository.findByStudentId(profileId).stream()
                .collect(Collectors.toMap(StudentProgress::getCourseId, Function.identity()));

        if (progressByCourse.isEmpty()) {
            return List.of();
        }

        Map<Long, CourseResponse> coursesById = courseClient.findAll().getData().stream()
                .filter(c -> progressByCourse.containsKey(c.getId()))
                .collect(Collectors.toMap(CourseResponse::getId, Function.identity()));

        List<Long> courseIds = new ArrayList<>(progressByCourse.keySet());
        Map<Long, List<AssignmentResponse>> assignmentsByCourse = assignmentRepository
                .findByCourseIdInOrderByDueDateAsc(courseIds).stream()
                .map(this::toAssignmentResponse)
                .collect(Collectors.groupingBy(AssignmentResponse::getCourseId));
        Map<Long, List<ExamResponse>> examsByCourse = examRepository
                .findByCourseIdInOrderByScheduledDateAsc(courseIds).stream()
                .map(this::toExamResponse)
                .collect(Collectors.groupingBy(ExamResponse::getCourseId));

        return progressByCourse.values().stream()
                .map(progress -> {
                    CourseResponse course = coursesById.get(progress.getCourseId());
                    if (course == null) {
                        return null;
                    }
                    return StudentEnrolledCourseResponse.builder()
                            .id(course.getId())
                            .title(course.getTitle())
                            .description(course.getDescription())
                            .department(course.getDepartment())
                            .instructor(course.getInstructor())
                            .credits(course.getCredits())
                            .progressPercent(progress.getProgressPercent())
                            .assignments(assignmentsByCourse.getOrDefault(course.getId(), List.of()))
                            .exams(examsByCourse.getOrDefault(course.getId(), List.of()))
                            .build();
                })
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(StudentEnrolledCourseResponse::getTitle,
                        Comparator.nullsLast(String::compareToIgnoreCase)))
                .toList();
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

    private void ensureStudentExists(Long profileId) {
        if (!studentRepository.existsById(profileId)) {
            throw new IllegalArgumentException("Student not found");
        }
    }

    private void ensureEnrolled(Long profileId, Long courseId) {
        ensureStudentExists(profileId);
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
