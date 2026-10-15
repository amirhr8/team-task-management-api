package com.portfolio.taskmanager.user.dto;

import com.portfolio.taskmanager.user.entity.Role;
import com.portfolio.taskmanager.user.entity.User;

import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String email,
        String fullName,
        Role role,
        LocalDateTime createdAt
) {
    public static UserResponse fromEntity(User user) {
        if (user == null) {
            return null;
        }
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getRole(),
                user.getCreatedAt()
        );
    }
}
