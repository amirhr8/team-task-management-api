package com.portfolio.taskmanager.task.dto;

import com.portfolio.taskmanager.task.entity.TaskStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateTaskStatusRequest(
        @NotNull(message = "Task status is required")
        TaskStatus status
) {
}
