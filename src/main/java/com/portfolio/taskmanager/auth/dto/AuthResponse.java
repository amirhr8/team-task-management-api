package com.portfolio.taskmanager.auth.dto;

import com.portfolio.taskmanager.user.dto.UserResponse;

public record AuthResponse(
        String accessToken,
        String tokenType,
        UserResponse user
) {
    public static AuthResponse of(String accessToken, UserResponse user) {
        return new AuthResponse(accessToken, "Bearer", user);
    }
}
