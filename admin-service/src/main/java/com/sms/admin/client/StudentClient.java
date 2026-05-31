package com.sms.admin.client;

import com.sms.common.dto.ApiResponse;
import com.sms.common.dto.StudentRequest;
import com.sms.common.dto.StudentResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "student-service")
public interface StudentClient {

    @PostMapping("/api/students/internal")
    ApiResponse<StudentResponse> create(@RequestBody StudentRequest request);

    @PutMapping("/api/students/internal/{id}")
    ApiResponse<StudentResponse> update(@PathVariable("id") Long id, @RequestBody StudentRequest request);

    @DeleteMapping("/api/students/internal/{id}")
    ApiResponse<Void> delete(@PathVariable("id") Long id);

    @GetMapping("/api/students/internal")
    ApiResponse<List<StudentResponse>> findAll();

    @PatchMapping("/api/students/internal/{id}/activate")
    ApiResponse<StudentResponse> activate(@PathVariable("id") Long id);
}
