package com.agroconnect.auth.service;

import com.agroconnect.auth.dto.AuthResponse;
import com.agroconnect.auth.dto.LoginRequest;
import com.agroconnect.auth.dto.RegisterRequest;
import com.agroconnect.auth.dto.UserResponse;
import com.agroconnect.auth.client.UserManagementClient;
import com.agroconnect.auth.client.dto.UserManagementSyncRequest;
import com.agroconnect.auth.entity.Role;
import com.agroconnect.auth.entity.User;
import com.agroconnect.auth.exception.ResourceConflictException;
import com.agroconnect.auth.exception.UnauthorizedException;
import com.agroconnect.auth.repository.UserRepository;
import com.agroconnect.auth.security.CustomUserPrincipal;
import com.agroconnect.auth.security.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserManagementClient userManagementClient;

    public AuthServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           AuthenticationManager authenticationManager,
                           JwtTokenProvider jwtTokenProvider,
                           UserManagementClient userManagementClient) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userManagementClient = userManagementClient;
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = request.getEmail().toLowerCase().trim();
        if (userRepository.existsByEmail(email)) {
            throw new ResourceConflictException("Email already exists");
        }
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new ResourceConflictException("Phone already exists");
        }
        if (request.getRole() == Role.ADMIN) {
            throw new UnauthorizedException("ADMIN role cannot be self-registered");
        }

        User user = new User();
        user.setName(request.getName().trim());
        user.setEmail(email);
        user.setPhone(request.getPhone().trim());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        user.setEnabled(true);

        User savedUser = userRepository.save(user);
        syncUserProfile(savedUser);
        return buildAuthResponse(new CustomUserPrincipal(savedUser), savedUser);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail().toLowerCase().trim(), request.getPassword())
            );

            CustomUserPrincipal principal = (CustomUserPrincipal) authentication.getPrincipal();
            User user = userRepository.findByEmail(principal.getUsername())
                    .orElseThrow(() -> new UnauthorizedException("User does not exist"));

            return buildAuthResponse(principal, user);
        } catch (BadCredentialsException ex) {
            throw new UnauthorizedException("Invalid email or password");
        }
    }

    @Override
    public UserResponse getCurrentUser(String email) {
        User user = userRepository.findByEmail(email.toLowerCase().trim())
                .orElseThrow(() -> new UnauthorizedException("User not found"));
        return toUserResponse(user);
    }

    private AuthResponse buildAuthResponse(CustomUserPrincipal principal, User user) {
        String token = jwtTokenProvider.generateToken(principal);
        return new AuthResponse(
            token,
            "Bearer",
            jwtTokenProvider.getExpirationMs(),
            toUserResponse(user)
        );
    }

    private void syncUserProfile(User user) {
        try {
            userManagementClient.syncProfile(new UserManagementSyncRequest(
                    user.getId(),
                    user.getName(),
                    user.getEmail(),
                    user.getPhone(),
                    user.getRole().name()
            ));
        } catch (Exception ex) {
            log.warn("Skipping profile sync for user {}: {}", user.getId(), ex.getMessage());
        }
    }

    private UserResponse toUserResponse(User user) {
        return new UserResponse(
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.getPhone(),
            user.getRole()
        );
    }
}
