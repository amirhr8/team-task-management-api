package com.portfolio.taskmanager.project.service;

import com.portfolio.taskmanager.common.exception.ResourceNotFoundException;
import com.portfolio.taskmanager.project.dto.CreateProjectRequest;
import com.portfolio.taskmanager.project.dto.ProjectResponse;
import com.portfolio.taskmanager.project.dto.UpdateProjectRequest;
import com.portfolio.taskmanager.project.entity.Project;
import com.portfolio.taskmanager.project.repository.ProjectRepository;
import com.portfolio.taskmanager.workspace.entity.Workspace;
import com.portfolio.taskmanager.workspace.repository.WorkspaceRepository;
import com.portfolio.taskmanager.workspace.service.WorkspaceAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceAccessService workspaceAccessService;

    @Transactional
    public ProjectResponse createProject(Long workspaceId, CreateProjectRequest request, Long currentUserId) {
        workspaceAccessService.validateMemberAccess(workspaceId, currentUserId);

        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace", "id", workspaceId));

        Project project = Project.builder()
                .name(request.name().trim())
                .description(request.description() != null ? request.description().trim() : null)
                .workspace(workspace)
                .build();

        Project savedProject = projectRepository.save(project);
        return ProjectResponse.fromEntity(savedProject);
    }

    @Transactional(readOnly = true)
    public ProjectResponse getProjectById(Long projectId, Long currentUserId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));

        workspaceAccessService.validateMemberAccess(project.getWorkspace().getId(), currentUserId);

        return ProjectResponse.fromEntity(project);
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> getProjectsByWorkspace(Long workspaceId, Long currentUserId) {
        workspaceAccessService.validateMemberAccess(workspaceId, currentUserId);

        return projectRepository.findByWorkspaceId(workspaceId)
                .stream()
                .map(ProjectResponse::fromEntity)
                .toList();
    }

    @Transactional
    public ProjectResponse updateProject(Long projectId, UpdateProjectRequest request, Long currentUserId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));

        workspaceAccessService.validateAdminOrOwnerAccess(project.getWorkspace().getId(), currentUserId);

        project.setName(request.name().trim());
        project.setDescription(request.description() != null ? request.description().trim() : null);

        Project updatedProject = projectRepository.save(project);
        return ProjectResponse.fromEntity(updatedProject);
    }

    @Transactional
    public void deleteProject(Long projectId, Long currentUserId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));

        workspaceAccessService.validateAdminOrOwnerAccess(project.getWorkspace().getId(), currentUserId);

        projectRepository.delete(project);
    }
}
