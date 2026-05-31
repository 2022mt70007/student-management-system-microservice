package com.sms.admin.client;

import com.sms.common.dto.ApiResponse;
import com.sms.common.dto.TeacherRequest;
import com.sms.common.dto.TeacherResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "teacher-service")
public interface TeacherClient {

    @PostMapping("/api/teachers/internal")
    ApiResponse<TeacherResponse> create(@RequestBody TeacherRequest request);

    @PutMapping("/api/teachers/internal/{id}")
    ApiResponse<TeacherResponse> update(@PathVariable("id") Long id, @RequestBody TeacherRequest request);

    @DeleteMapping("/api/teachers/internal/{id}")
    ApiResponse<Void> delete(@PathVariable("id") Long id);

    @GetMapping("/api/teachers/internal")
    ApiResponse<List<TeacherResponse>> findAll();

    @PatchMapping("/api/teachers/internal/{id}/activate")
    ApiResponse<TeacherResponse> activate(@PathVariable("id") Long id);
}
