package com.sms.student.client;

import com.sms.common.dto.ApiResponse;
import com.sms.common.dto.TeacherResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "teacher-service")
public interface TeacherClient {

    @GetMapping("/api/teachers/internal/by-academic")
    ApiResponse<List<TeacherResponse>> findByDepartmentAndClass(
            @RequestParam("departmentId") Long departmentId,
            @RequestParam("classId") Long classId);
}
