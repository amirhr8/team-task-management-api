package com.portfolio.taskmanager.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        Map<String, String> validationErrors
) {
    public static ApiErrorResponse of(int status, String error, String message, String path) {
        return new ApiErrorResponse(LocalDateTime.now(), status, error, message, path, null);
    }

    public static ApiErrorResponse of(int status, String error, String message, String path, Map<String, String> validationErrors) {
        return new ApiErrorResponse(LocalDateTime.now(), status, error, message, path, validationErrors);
    }
}
