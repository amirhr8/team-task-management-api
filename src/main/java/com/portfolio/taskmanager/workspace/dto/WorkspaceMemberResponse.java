package com.portfolio.taskmanager.workspace.dto;

import com.portfolio.taskmanager.workspace.entity.WorkspaceMember;
import com.portfolio.taskmanager.workspace.entity.WorkspaceRole;

import java.time.LocalDateTime;

public record WorkspaceMemberResponse(
        Long userId,
        String email,
        String fullName,
        WorkspaceRole roleInWorkspace,
        LocalDateTime joinedAt
) {
    public static WorkspaceMemberResponse fromEntity(WorkspaceMember member) {
        if (member == null) {
            return null;
        }
        return new WorkspaceMemberResponse(
                member.getUser().getId(),
                member.getUser().getEmail(),
                member.getUser().getFullName(),
                member.getRoleInWorkspace(),
                member.getJoinedAt()
        );
    }
}
