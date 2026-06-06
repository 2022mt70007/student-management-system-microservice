package com.sms.course.service;

import com.sms.common.dto.CourseRequest;
import com.sms.common.dto.CourseResponse;
import com.sms.common.security.InputSanitizer;
import com.sms.course.entity.Course;
import com.sms.course.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;

    @Transactional
    public CourseResponse create(CourseRequest request) {
        Course course = Course.builder()
                .title(InputSanitizer.cleanText(request.getTitle()))
                .description(InputSanitizer.cleanText(request.getDescription()))
                .department(InputSanitizer.cleanText(request.getDepartment()))
                .instructor(InputSanitizer.cleanText(request.getInstructor()))
                .credits(request.getCredits())
                .build();
        return toResponse(courseRepository.save(course));
    }

    @Transactional
    public CourseResponse update(Long id, CourseRequest request) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));

        course.setTitle(InputSanitizer.cleanText(request.getTitle()));
        course.setDescription(InputSanitizer.cleanText(request.getDescription()));
        course.setDepartment(InputSanitizer.cleanText(request.getDepartment()));
        course.setInstructor(InputSanitizer.cleanText(request.getInstructor()));
        course.setCredits(request.getCredits());

        return toResponse(courseRepository.save(course));
    }

    @Transactional
    public void delete(Long id) {
        if (!courseRepository.existsById(id)) {
            throw new IllegalArgumentException("Course not found");
        }
        courseRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<CourseResponse> findAll() {
        return courseRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public CourseResponse findById(Long id) {
        return toResponse(courseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Course not found")));
    }

    private CourseResponse toResponse(Course course) {
        return CourseResponse.builder()
                .id(course.getId())
                .title(course.getTitle())
                .description(course.getDescription())
                .department(course.getDepartment())
                .instructor(course.getInstructor())
                .credits(course.getCredits())
                .build();
    }
}
