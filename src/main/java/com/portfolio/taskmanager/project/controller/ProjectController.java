package com.portfolio.taskmanager.project.controller;

import com.portfolio.taskmanager.project.dto.CreateProjectRequest;
import com.portfolio.taskmanager.project.dto.ProjectResponse;
import com.portfolio.taskmanager.project.dto.UpdateProjectRequest;
import com.portfolio.taskmanager.project.service.ProjectService;
import com.portfolio.taskmanager.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Projects", description = "Project management endpoints within workspaces")
@SecurityRequirement(name = "BearerAuth")
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping("/workspaces/{workspaceId}/projects")
    @Operation(summary = "Create project", description = "Creates a new project inside the specified workspace")
    @ApiResponse(responseCode = "201", description = "Project created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request payload")
    @ApiResponse(responseCode = "403", description = "Access denied: not a workspace member")
    @ApiResponse(responseCode = "404", description = "Workspace not found")
    public ResponseEntity<ProjectResponse> createProject(
            @PathVariable Long workspaceId,
            @Valid @RequestBody CreateProjectRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        ProjectResponse response = projectService.createProject(workspaceId, request, userDetails.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/workspaces/{workspaceId}/projects")
    @Operation(summary = "List projects in workspace", description = "Retrieves all projects belonging to the specified workspace")
    @ApiResponse(responseCode = "200", description = "Projects retrieved successfully")
    @ApiResponse(responseCode = "403", description = "Access denied: not a workspace member")
    public ResponseEntity<List<ProjectResponse>> getProjectsByWorkspace(
            @PathVariable Long workspaceId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<ProjectResponse> response = projectService.getProjectsByWorkspace(workspaceId, userDetails.getId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/projects/{projectId}")
    @Operation(summary = "Get project details", description = "Retrieves project details by project ID")
    @ApiResponse(responseCode = "200", description = "Project retrieved successfully")
    @ApiResponse(responseCode = "403", description = "Access denied: not a workspace member")
    @ApiResponse(responseCode = "404", description = "Project not found")
    public ResponseEntity<ProjectResponse> getProjectById(
            @PathVariable Long projectId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        ProjectResponse response = projectService.getProjectById(projectId, userDetails.getId());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/projects/{projectId}")
    @Operation(summary = "Update project", description = "Updates project metadata (ADMIN or OWNER only)")
    @ApiResponse(responseCode = "200", description = "Project updated successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request payload")
    @ApiResponse(responseCode = "403", description = "Access denied: requires ADMIN or OWNER role")
    @ApiResponse(responseCode = "404", description = "Project not found")
    public ResponseEntity<ProjectResponse> updateProject(
            @PathVariable Long projectId,
            @Valid @RequestBody UpdateProjectRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        ProjectResponse response = projectService.updateProject(projectId, request, userDetails.getId());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/projects/{projectId}")
    @Operation(summary = "Delete project", description = "Deletes a project and its tasks (ADMIN or OWNER only)")
    @ApiResponse(responseCode = "204", description = "Project deleted successfully")
    @ApiResponse(responseCode = "403", description = "Access denied: requires ADMIN or OWNER role")
    @ApiResponse(responseCode = "404", description = "Project not found")
    public ResponseEntity<Void> deleteProject(
            @PathVariable Long projectId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        projectService.deleteProject(projectId, userDetails.getId());
        return ResponseEntity.noContent().build();
    }
}
