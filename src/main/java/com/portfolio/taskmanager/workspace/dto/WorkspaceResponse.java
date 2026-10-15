package com.portfolio.taskmanager.workspace.dto;

import com.portfolio.taskmanager.user.dto.UserResponse;
import com.portfolio.taskmanager.workspace.entity.Workspace;

import java.time.LocalDateTime;

public record WorkspaceResponse(
        Long id,
        String name,
        String description,
        UserResponse owner,
        LocalDateTime createdAt
) {
    public static WorkspaceResponse fromEntity(Workspace workspace) {
        if (workspace == null) {
            return null;
        }
        return new WorkspaceResponse(
                workspace.getId(),
                workspace.getName(),
                workspace.getDescription(),
                UserResponse.fromEntity(workspace.getOwner()),
                workspace.getCreatedAt()
        );
    }
}
