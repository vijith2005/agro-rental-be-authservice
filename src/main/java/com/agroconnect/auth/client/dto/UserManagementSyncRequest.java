package com.agroconnect.auth.client.dto;

public record UserManagementSyncRequest(
        Long authUserId,
        String name,
        String email,
        String phone,
        String role
) {
}
