package com.agroconnect.auth.client;

import com.agroconnect.auth.client.dto.UserManagementStatusRequest;
import com.agroconnect.auth.client.dto.UserManagementSyncRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "UserManagementService",
        url = "${user-management.service.url:http://localhost:8082}",
        path = "/api/v1/user-management"
)
public interface UserManagementClient {

    @PostMapping("/profiles/sync")
    void syncProfile(@RequestBody UserManagementSyncRequest request);

    @PatchMapping("/profiles/{authUserId}/status")
    void updateStatus(@PathVariable("authUserId") Long authUserId, @RequestBody UserManagementStatusRequest request);
}
