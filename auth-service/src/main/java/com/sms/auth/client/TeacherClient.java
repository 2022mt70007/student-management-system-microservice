package com.sms.auth.client;

import com.sms.common.dto.ApiResponse;
import com.sms.common.dto.TeacherResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "teacher-service")
public interface TeacherClient {

    @PatchMapping("/api/teachers/internal/{id}/activate")
    ApiResponse<TeacherResponse> activate(@PathVariable("id") Long id);
}
