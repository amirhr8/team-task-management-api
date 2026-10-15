package com.portfolio.taskmanager.workspace.dto;

import com.portfolio.taskmanager.workspace.entity.WorkspaceRole;
import jakarta.validation.constraints.NotNull;

public record AddWorkspaceMemberRequest(
        @NotNull(message = "User ID is required")
        Long userId,

        @NotNull(message = "Role is required")
        WorkspaceRole role
) {
}
