package com.sms.teacher.client;

import com.sms.common.dto.ApiResponse;
import com.sms.common.dto.NotificationResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "notification-service")
public interface NotificationClient {

    @GetMapping("/api/notifications")
    ApiResponse<List<NotificationResponse>> findAll(@RequestParam(value = "role", required = false) String role);
}
