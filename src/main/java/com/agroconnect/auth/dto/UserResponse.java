package com.agroconnect.auth.dto;

import com.agroconnect.auth.entity.Role;

public class UserResponse {
    private final Long id;
    private final String name;
    private final String email;
    private final String phone;
    private final Role role;

    public UserResponse(Long id, String name, String email, String phone, Role role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public Role getRole() {
        return role;
    }
}

