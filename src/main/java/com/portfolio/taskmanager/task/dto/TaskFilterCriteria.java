package com.portfolio.taskmanager.task.dto;

import com.portfolio.taskmanager.task.entity.TaskPriority;
import com.portfolio.taskmanager.task.entity.TaskStatus;

public record TaskFilterCriteria(
        TaskStatus status,
        TaskPriority priority,
        Long assignedToId
) {
}
