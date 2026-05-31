package com.sms.auth.client;

import com.sms.common.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "admin-service")
public interface AdminClient {

    @PatchMapping("/api/admin/internal/{id}/activate")
    ApiResponse<Void> activate(@PathVariable("id") Long id);
}
