package com.sms.student.client;

import com.sms.common.dto.AcademicSelectionRequest;
import com.sms.common.dto.AcademicSelectionResponse;
import com.sms.common.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "course-service", contextId = "studentAcademicClient")
public interface AcademicClient {

    @PostMapping("/api/academic/validate-selection")
    ApiResponse<AcademicSelectionResponse> validateSelection(@RequestBody AcademicSelectionRequest request);
}
