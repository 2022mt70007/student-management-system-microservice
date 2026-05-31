package com.sms.admin.client;

import com.sms.common.dto.ApiResponse;
import com.sms.common.dto.CreateInvitationRequest;
import com.sms.common.dto.InvitationResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "auth-service")
public interface AuthClient {

    @PostMapping("/api/auth/invitations")
    ApiResponse<InvitationResponse> createInvitation(@RequestBody CreateInvitationRequest request);
}
