package com.portfolio.taskmanager.task.dto;

import com.portfolio.taskmanager.task.entity.TaskPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateTaskRequest(
        @NotBlank(message = "Task title is required")
        @Size(min = 2, max = 200, message = "Task title must be between 2 and 200 characters")
        String title,

        String description,

        @NotNull(message = "Task priority is required")
        TaskPriority priority,

        LocalDate dueDate,

        Long assignedToId
) {
}
