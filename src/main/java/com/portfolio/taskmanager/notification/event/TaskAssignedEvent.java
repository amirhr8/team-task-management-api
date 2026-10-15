package com.portfolio.taskmanager.notification.event;

public record TaskAssignedEvent(
        Long taskId,
        String taskTitle,
        Long assignedUserId,
        String assignedUserEmail,
        Long assignedByUserId
) {
}
