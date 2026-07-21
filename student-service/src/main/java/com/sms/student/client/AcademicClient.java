package com.sms.student.client;

import com.sms.common.dto.AcademicSelectionRequest;
import com.sms.common.dto.AcademicSelectionResponse;
import com.sms.common.dto.ApiResponse;
import com.sms.common.dto.SubjectResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "course-service", contextId = "studentAcademicClient")
public interface AcademicClient {

    @PostMapping("/api/academic/validate-selection")
    ApiResponse<AcademicSelectionResponse> validateSelection(@RequestBody AcademicSelectionRequest request);

    @PostMapping("/api/academic/internal/subjects/by-ids")
    ApiResponse<List<SubjectResponse>> findSubjectsByIds(@RequestBody List<Long> ids);
}
