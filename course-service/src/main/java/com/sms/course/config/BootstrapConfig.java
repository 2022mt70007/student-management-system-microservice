package com.sms.course.config;

import com.sms.course.entity.Course;
import com.sms.course.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class BootstrapConfig {

    private final CourseRepository courseRepository;

    @Bean
    CommandLineRunner seedCourses() {
        return args -> {
            if (courseRepository.count() == 0) {
                courseRepository.save(Course.builder()
                        .title("Introduction to Computer Science")
                        .description("Fundamentals of programming and algorithms")
                        .department("Computer Science")
                        .instructor("Dr. Smith")
                        .credits(4)
                        .build());
                courseRepository.save(Course.builder()
                        .title("Data Structures")
                        .description("Arrays, trees, graphs, and complexity analysis")
                        .department("Computer Science")
                        .instructor("Dr. Johnson")
                        .credits(3)
                        .build());
            }
        };
    }
}
