package com.sms.student.config;

import com.sms.common.dto.CourseResponse;
import com.sms.student.client.CourseClient;
import com.sms.student.entity.Assignment;
import com.sms.student.entity.Exam;
import com.sms.student.repository.AssignmentRepository;
import com.sms.student.repository.ExamRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.util.List;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class BootstrapConfig {

    private final AssignmentRepository assignmentRepository;
    private final ExamRepository examRepository;
    private final CourseClient courseClient;

    @Bean
    CommandLineRunner seedAssignmentsAndExams() {
        return args -> {
            if (assignmentRepository.count() > 0) {
                return;
            }

            List<CourseResponse> courses;
            try {
                courses = courseClient.findAll().getData();
            } catch (Exception e) {
                log.warn("Could not fetch courses for assignment seeding: {}", e.getMessage());
                return;
            }

            if (courses == null || courses.isEmpty()) {
                return;
            }

            LocalDate today = LocalDate.now();
            for (CourseResponse course : courses) {
                Long courseId = course.getId();
                String title = course.getTitle() != null ? course.getTitle() : "Course";

                assignmentRepository.save(Assignment.builder()
                        .courseId(courseId)
                        .title(title + " — Problem Set 1")
                        .description("Complete exercises from chapters 1–3.")
                        .dueDate(today.plusDays(3))
                        .build());
                assignmentRepository.save(Assignment.builder()
                        .courseId(courseId)
                        .title(title + " — Lab Report")
                        .description("Submit your lab findings and analysis.")
                        .dueDate(today.minusDays(2))
                        .build());
                assignmentRepository.save(Assignment.builder()
                        .courseId(courseId)
                        .title(title + " — Final Project")
                        .description("Capstone project for the semester.")
                        .dueDate(today.plusDays(21))
                        .build());

                examRepository.save(Exam.builder()
                        .courseId(courseId)
                        .title(title + " — Midterm Exam")
                        .description("Covers material from the first half of the course.")
                        .scheduledDate(today.plusDays(5))
                        .build());
                examRepository.save(Exam.builder()
                        .courseId(courseId)
                        .title(title + " — Final Exam")
                        .description("Comprehensive end-of-term examination.")
                        .scheduledDate(today.plusDays(30))
                        .build());
            }
        };
    }
}
