package com.agroconnect.auth.service;

import com.agroconnect.auth.dto.AuthResponse;
import com.agroconnect.auth.dto.ChangePasswordRequest;
import com.agroconnect.auth.dto.UpdateProfileRequest;
import com.agroconnect.auth.dto.UpdateUserStatusRequest;
import com.agroconnect.auth.dto.UserResponse;
import java.util.List;

public interface UserService {

    UserResponse getById(Long id);

    AuthResponse updateMyProfile(String email, UpdateProfileRequest request);

    void changeMyPassword(String email, ChangePasswordRequest request);

    List<UserResponse> getAllUsers();

    UserResponse updateUserStatus(Long id, UpdateUserStatusRequest request);
}
