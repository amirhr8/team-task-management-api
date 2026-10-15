package com.portfolio.taskmanager.project.dto;

import com.portfolio.taskmanager.project.entity.Project;

import java.time.LocalDateTime;

public record ProjectResponse(
        Long id,
        String name,
        String description,
        Long workspaceId,
        LocalDateTime createdAt
) {
    public static ProjectResponse fromEntity(Project project) {
        if (project == null) {
            return null;
        }
        return new ProjectResponse(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getWorkspace() != null ? project.getWorkspace().getId() : null,
                project.getCreatedAt()
        );
    }
}
