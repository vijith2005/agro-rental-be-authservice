package com.agroconnect.auth.service;

import com.agroconnect.auth.dto.AuthResponse;
import com.agroconnect.auth.dto.ChangePasswordRequest;
import com.agroconnect.auth.dto.UpdateProfileRequest;
import com.agroconnect.auth.dto.UpdateUserStatusRequest;
import com.agroconnect.auth.dto.UserResponse;
import com.agroconnect.auth.entity.User;
import com.agroconnect.auth.exception.BadRequestException;
import com.agroconnect.auth.exception.ResourceConflictException;
import com.agroconnect.auth.exception.UnauthorizedException;
import com.agroconnect.auth.repository.UserRepository;
import com.agroconnect.auth.security.CustomUserPrincipal;
import com.agroconnect.auth.security.JwtTokenProvider;
import java.util.List;
 
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public UserServiceImpl(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public UserResponse getById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("User not found"));
        return toUserResponse(user);
    }

    @Override
    @Transactional
    public AuthResponse updateMyProfile(String email, UpdateProfileRequest request) {
        User user = userRepository.findByEmail(email.toLowerCase().trim())
                .orElseThrow(() -> new UnauthorizedException("User not found"));
        String incomingPhone = request.getPhone().trim();
        if (!incomingPhone.equals(user.getPhone()) && userRepository.existsByPhone(incomingPhone)) {
            throw new ResourceConflictException("Phone already exists");
        }

        String incomingEmail = request.getEmail().toLowerCase().trim();
        if (!incomingEmail.equals(user.getEmail()) && userRepository.existsByEmail(incomingEmail)) {
            throw new ResourceConflictException("Email already exists");
        }

        user.setName(request.getName().trim());
        user.setPhone(incomingPhone);
        user.setEmail(incomingEmail);
        User savedUser = userRepository.save(user);
        CustomUserPrincipal principal = new CustomUserPrincipal(savedUser);
        return new AuthResponse(
            jwtTokenProvider.generateToken(principal),
            "Bearer",
            jwtTokenProvider.getExpirationMs(),
            toUserResponse(savedUser)
        );
    }

    @Override
    @Transactional
    public void changeMyPassword(String email, ChangePasswordRequest request) {
        User user = userRepository.findByEmail(email.toLowerCase().trim())
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException("Current password is incorrect");
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("New password and confirm password do not match");
        }

        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new BadRequestException("New password must be different from current password");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::toUserResponse)
                .toList();
    }

    @Override
    @Transactional
    public UserResponse updateUserStatus(Long id, UpdateUserStatusRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("User not found"));
        user.setEnabled(request.getEnabled());
        user.setRole(request.getRole());
        return toUserResponse(userRepository.save(user));
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
