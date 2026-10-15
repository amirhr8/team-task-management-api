package com.portfolio.taskmanager.task.dto;

import com.portfolio.taskmanager.task.entity.Task;
import com.portfolio.taskmanager.task.entity.TaskPriority;
import com.portfolio.taskmanager.task.entity.TaskStatus;
import com.portfolio.taskmanager.user.dto.UserResponse;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record TaskResponse(
        Long id,
        String title,
        String description,
        TaskStatus status,
        TaskPriority priority,
        LocalDate dueDate,
        Long projectId,
        UserResponse assignedTo,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static TaskResponse fromEntity(Task task) {
        if (task == null) {
            return null;
        }
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getPriority(),
                task.getDueDate(),
                task.getProject() != null ? task.getProject().getId() : null,
                task.getAssignedTo() != null ? UserResponse.fromEntity(task.getAssignedTo()) : null,
                task.getCreatedAt(),
                task.getUpdatedAt()
        );
    }
}
