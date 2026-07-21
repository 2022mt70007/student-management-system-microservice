package com.sms.teacher.client;

import com.sms.common.dto.ApiResponse;
import com.sms.common.dto.StudentResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "student-service")
public interface StudentClient {

    @GetMapping("/api/students/internal/by-academic")
    ApiResponse<List<StudentResponse>> findByDepartmentAndClass(
            @RequestParam("departmentId") Long departmentId,
            @RequestParam("classId") Long classId);
}
