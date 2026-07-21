package com.sms.auth.client;

import com.sms.common.dto.ApiResponse;
import com.sms.common.dto.StudentResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "student-service")
public interface StudentClient {

    @PostMapping("/api/students/internal/{id}/activate")
    ApiResponse<StudentResponse> activate(@PathVariable("id") Long id);
}
