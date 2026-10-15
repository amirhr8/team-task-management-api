package com.portfolio.taskmanager.workspace.service;

import com.portfolio.taskmanager.common.exception.ForbiddenException;
import com.portfolio.taskmanager.common.exception.ResourceNotFoundException;
import com.portfolio.taskmanager.workspace.entity.WorkspaceMember;
import com.portfolio.taskmanager.workspace.entity.WorkspaceRole;
import com.portfolio.taskmanager.workspace.repository.WorkspaceMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WorkspaceAccessService {

    private final WorkspaceMemberRepository workspaceMemberRepository;

    @Transactional(readOnly = true)
    public boolean isMember(Long workspaceId, Long userId) {
        if (workspaceId == null || userId == null) {
            return false;
        }
        return workspaceMemberRepository.existsByWorkspaceIdAndUserId(workspaceId, userId);
    }

    @Transactional(readOnly = true)
    public WorkspaceMember getMember(Long workspaceId, Long userId) {
        return workspaceMemberRepository.findByWorkspaceIdAndUserId(workspaceId, userId)
                .orElseThrow(() -> new ForbiddenException(
                        String.format("Access denied: User %d is not a member of workspace %d", userId, workspaceId)
                ));
    }

    @Transactional(readOnly = true)
    public void validateMemberAccess(Long workspaceId, Long userId) {
        if (!isMember(workspaceId, userId)) {
            throw new ForbiddenException(
                    String.format("Access denied: User %d is not a member of workspace %d", userId, workspaceId)
            );
        }
    }

    @Transactional(readOnly = true)
    public void validateAdminOrOwnerAccess(Long workspaceId, Long userId) {
        WorkspaceMember member = getMember(workspaceId, userId);
        if (member.getRoleInWorkspace() != WorkspaceRole.OWNER && member.getRoleInWorkspace() != WorkspaceRole.ADMIN) {
            throw new ForbiddenException(
                    String.format("Access denied: Admin or Owner role required in workspace %d", workspaceId)
            );
        }
    }

    @Transactional(readOnly = true)
    public void validateOwnerAccess(Long workspaceId, Long userId) {
        WorkspaceMember member = getMember(workspaceId, userId);
        if (member.getRoleInWorkspace() != WorkspaceRole.OWNER) {
            throw new ForbiddenException(
                    String.format("Access denied: Owner role required in workspace %d", workspaceId)
            );
        }
    }
}
