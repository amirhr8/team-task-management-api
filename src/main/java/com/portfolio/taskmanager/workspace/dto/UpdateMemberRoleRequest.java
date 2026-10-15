package com.portfolio.taskmanager.workspace.dto;

import com.portfolio.taskmanager.workspace.entity.WorkspaceRole;
import jakarta.validation.constraints.NotNull;

public record UpdateMemberRoleRequest(
        @NotNull(message = "Role is required")
        WorkspaceRole role
) {
}
