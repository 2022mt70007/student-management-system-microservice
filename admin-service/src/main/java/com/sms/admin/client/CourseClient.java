package com.sms.admin.client;

import com.sms.common.dto.ApiResponse;
import com.sms.common.dto.CourseRequest;
import com.sms.common.dto.CourseResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "course-service", contextId = "adminCourseClient")
public interface CourseClient {

    @PostMapping("/api/courses")
    ApiResponse<CourseResponse> create(@RequestBody CourseRequest request);

    @PutMapping("/api/courses/{id}")
    ApiResponse<CourseResponse> update(@PathVariable("id") Long id, @RequestBody CourseRequest request);

    @DeleteMapping("/api/courses/{id}")
    ApiResponse<Void> delete(@PathVariable("id") Long id);

    @GetMapping("/api/courses")
    ApiResponse<List<CourseResponse>> findAll();
}
