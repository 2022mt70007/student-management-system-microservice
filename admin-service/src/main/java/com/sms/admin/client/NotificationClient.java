package com.sms.admin.client;

import com.sms.common.dto.ApiResponse;
import com.sms.common.dto.NotificationRequest;
import com.sms.common.dto.NotificationResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "notification-service")
public interface NotificationClient {

    @PostMapping("/api/notifications")
    ApiResponse<NotificationResponse> create(@RequestBody NotificationRequest request);

    @PutMapping("/api/notifications/{id}")
    ApiResponse<NotificationResponse> update(@PathVariable("id") Long id, @RequestBody NotificationRequest request);

    @DeleteMapping("/api/notifications/{id}")
    ApiResponse<Void> delete(@PathVariable("id") Long id);

    @GetMapping("/api/notifications")
    ApiResponse<List<NotificationResponse>> findAll();
}
