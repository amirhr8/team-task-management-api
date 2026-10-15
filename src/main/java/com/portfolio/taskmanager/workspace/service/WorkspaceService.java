package com.portfolio.taskmanager.workspace.service;

import com.portfolio.taskmanager.common.exception.BadRequestException;
import com.portfolio.taskmanager.common.exception.ConflictException;
import com.portfolio.taskmanager.common.exception.ResourceNotFoundException;
import com.portfolio.taskmanager.user.entity.User;
import com.portfolio.taskmanager.user.repository.UserRepository;
import com.portfolio.taskmanager.workspace.dto.AddWorkspaceMemberRequest;
import com.portfolio.taskmanager.workspace.dto.CreateWorkspaceRequest;
import com.portfolio.taskmanager.workspace.dto.UpdateMemberRoleRequest;
import com.portfolio.taskmanager.workspace.dto.UpdateWorkspaceRequest;
import com.portfolio.taskmanager.workspace.dto.WorkspaceMemberResponse;
import com.portfolio.taskmanager.workspace.dto.WorkspaceResponse;
import com.portfolio.taskmanager.workspace.entity.Workspace;
import com.portfolio.taskmanager.workspace.entity.WorkspaceMember;
import com.portfolio.taskmanager.workspace.entity.WorkspaceRole;
import com.portfolio.taskmanager.workspace.repository.WorkspaceMemberRepository;
import com.portfolio.taskmanager.workspace.repository.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkspaceService {

    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final UserRepository userRepository;
    private final WorkspaceAccessService workspaceAccessService;

    @Transactional
    public WorkspaceResponse createWorkspace(CreateWorkspaceRequest request, Long userId) {
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Workspace workspace = Workspace.builder()
                .name(request.name().trim())
                .description(request.description() != null ? request.description().trim() : null)
                .owner(owner)
                .build();

        Workspace savedWorkspace = workspaceRepository.save(workspace);

        // Rule: Automatically add creator as WorkspaceMember with OWNER role
        WorkspaceMember ownerMember = new WorkspaceMember(savedWorkspace, owner, WorkspaceRole.OWNER);
        workspaceMemberRepository.save(ownerMember);

        return WorkspaceResponse.fromEntity(savedWorkspace);
    }

    @Transactional(readOnly = true)
    public WorkspaceResponse getWorkspaceById(Long workspaceId, Long userId) {
        workspaceAccessService.validateMemberAccess(workspaceId, userId);

        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace", "id", workspaceId));

        return WorkspaceResponse.fromEntity(workspace);
    }

    @Transactional(readOnly = true)
    public List<WorkspaceResponse> getAllUserWorkspaces(Long userId) {
        return workspaceRepository.findAllAccessibleByUserId(userId)
                .stream()
                .map(WorkspaceResponse::fromEntity)
                .toList();
    }

    @Transactional
    public WorkspaceResponse updateWorkspace(Long workspaceId, UpdateWorkspaceRequest request, Long userId) {
        workspaceAccessService.validateAdminOrOwnerAccess(workspaceId, userId);

        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace", "id", workspaceId));

        workspace.setName(request.name().trim());
        workspace.setDescription(request.description() != null ? request.description().trim() : null);

        Workspace updatedWorkspace = workspaceRepository.save(workspace);
        return WorkspaceResponse.fromEntity(updatedWorkspace);
    }

    @Transactional
    public void deleteWorkspace(Long workspaceId, Long userId) {
        workspaceAccessService.validateOwnerAccess(workspaceId, userId);

        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace", "id", workspaceId));

        workspaceRepository.delete(workspace);
    }

    @Transactional
    public WorkspaceMemberResponse addMember(Long workspaceId, AddWorkspaceMemberRequest request, Long currentUserId) {
        workspaceAccessService.validateAdminOrOwnerAccess(workspaceId, currentUserId);

        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace", "id", workspaceId));

        if (workspaceMemberRepository.existsByWorkspaceIdAndUserId(workspaceId, request.userId())) {
            throw new ConflictException(
                    String.format("User %d is already a member of workspace %d", request.userId(), workspaceId)
            );
        }

        User targetUser = userRepository.findById(request.userId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.userId()));

        WorkspaceMember newMember = new WorkspaceMember(workspace, targetUser, request.role());
        WorkspaceMember savedMember = workspaceMemberRepository.save(newMember);

        return WorkspaceMemberResponse.fromEntity(savedMember);
    }

    @Transactional
    public void removeMember(Long workspaceId, Long targetUserId, Long currentUserId) {
        workspaceAccessService.validateAdminOrOwnerAccess(workspaceId, currentUserId);

        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace", "id", workspaceId));

        if (workspace.getOwner().getId().equals(targetUserId)) {
            throw new BadRequestException("Cannot remove the workspace owner from the workspace");
        }

        WorkspaceMember member = workspaceMemberRepository.findByWorkspaceIdAndUserId(workspaceId, targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("User %d is not a member of workspace %d", targetUserId, workspaceId)
                ));

        workspaceMemberRepository.delete(member);
    }

    @Transactional
    public WorkspaceMemberResponse updateMemberRole(
            Long workspaceId,
            Long targetUserId,
            UpdateMemberRoleRequest request,
            Long currentUserId
    ) {
        workspaceAccessService.validateAdminOrOwnerAccess(workspaceId, currentUserId);

        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace", "id", workspaceId));

        if (workspace.getOwner().getId().equals(targetUserId)) {
            throw new BadRequestException("Cannot modify the role of the workspace owner");
        }

        WorkspaceMember member = workspaceMemberRepository.findByWorkspaceIdAndUserId(workspaceId, targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("User %d is not a member of workspace %d", targetUserId, workspaceId)
                ));

        member.setRoleInWorkspace(request.role());
        WorkspaceMember updatedMember = workspaceMemberRepository.save(member);

        return WorkspaceMemberResponse.fromEntity(updatedMember);
    }

    @Transactional(readOnly = true)
    public List<WorkspaceMemberResponse> getWorkspaceMembers(Long workspaceId, Long currentUserId) {
        workspaceAccessService.validateMemberAccess(workspaceId, currentUserId);

        return workspaceMemberRepository.findByWorkspaceId(workspaceId)
                .stream()
                .map(WorkspaceMemberResponse::fromEntity)
                .toList();
    }
}
