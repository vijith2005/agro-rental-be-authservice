package com.agroconnect.auth.service;

import com.agroconnect.auth.dto.AuthResponse;
import com.agroconnect.auth.dto.LoginRequest;
import com.agroconnect.auth.dto.RegisterRequest;
import com.agroconnect.auth.dto.UserResponse;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    UserResponse getCurrentUser(String email);
}

