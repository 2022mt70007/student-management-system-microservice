package com.sms.teacher.client;

import com.sms.common.dto.ApiResponse;
import com.sms.common.dto.CourseResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(name = "course-service")
public interface CourseClient {

    @GetMapping("/api/courses")
    ApiResponse<List<CourseResponse>> findAll();
}
