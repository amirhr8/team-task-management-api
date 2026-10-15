package com.portfolio.taskmanager.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateProjectRequest(
        @NotBlank(message = "Project name is required")
        @Size(min = 2, max = 100, message = "Project name must be between 2 and 100 characters")
        String name,

        @Size(max = 500, message = "Description must not exceed 500 characters")
        String description
) {
}
