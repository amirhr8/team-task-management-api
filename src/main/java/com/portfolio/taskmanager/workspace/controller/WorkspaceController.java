package com.portfolio.taskmanager.workspace.controller;

import com.portfolio.taskmanager.security.CustomUserDetails;
import com.portfolio.taskmanager.workspace.dto.AddWorkspaceMemberRequest;
import com.portfolio.taskmanager.workspace.dto.CreateWorkspaceRequest;
import com.portfolio.taskmanager.workspace.dto.UpdateMemberRoleRequest;
import com.portfolio.taskmanager.workspace.dto.UpdateWorkspaceRequest;
import com.portfolio.taskmanager.workspace.dto.WorkspaceMemberResponse;
import com.portfolio.taskmanager.workspace.dto.WorkspaceResponse;
import com.portfolio.taskmanager.workspace.service.WorkspaceService;
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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/workspaces")
@RequiredArgsConstructor
@Tag(name = "Workspaces", description = "Workspace and membership management endpoints")
@SecurityRequirement(name = "BearerAuth")
public class WorkspaceController {

    private final WorkspaceService workspaceService;

    @PostMapping
    @Operation(summary = "Create workspace", description = "Creates a new workspace and assigns OWNER role to the creator")
    @ApiResponse(responseCode = "201", description = "Workspace created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request payload")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    public ResponseEntity<WorkspaceResponse> createWorkspace(
            @Valid @RequestBody CreateWorkspaceRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        WorkspaceResponse response = workspaceService.createWorkspace(request, userDetails.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "List workspaces", description = "Retrieves all workspaces accessible to the authenticated user")
    @ApiResponse(responseCode = "200", description = "Workspaces retrieved successfully")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    public ResponseEntity<List<WorkspaceResponse>> getAllUserWorkspaces(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<WorkspaceResponse> response = workspaceService.getAllUserWorkspaces(userDetails.getId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{workspaceId}")
    @Operation(summary = "Get workspace details", description = "Retrieves workspace details if user is a member")
    @ApiResponse(responseCode = "200", description = "Workspace retrieved successfully")
    @ApiResponse(responseCode = "403", description = "Access denied: not a workspace member")
    @ApiResponse(responseCode = "404", description = "Workspace not found")
    public ResponseEntity<WorkspaceResponse> getWorkspaceById(
            @PathVariable Long workspaceId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        WorkspaceResponse response = workspaceService.getWorkspaceById(workspaceId, userDetails.getId());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{workspaceId}")
    @Operation(summary = "Update workspace", description = "Updates workspace metadata (ADMIN or OWNER only)")
    @ApiResponse(responseCode = "200", description = "Workspace updated successfully")
    @ApiResponse(responseCode = "403", description = "Access denied: requires ADMIN or OWNER role")
    @ApiResponse(responseCode = "404", description = "Workspace not found")
    public ResponseEntity<WorkspaceResponse> updateWorkspace(
            @PathVariable Long workspaceId,
            @Valid @RequestBody UpdateWorkspaceRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        WorkspaceResponse response = workspaceService.updateWorkspace(workspaceId, request, userDetails.getId());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{workspaceId}")
    @Operation(summary = "Delete workspace", description = "Deletes a workspace (OWNER only)")
    @ApiResponse(responseCode = "204", description = "Workspace deleted successfully")
    @ApiResponse(responseCode = "403", description = "Access denied: requires OWNER role")
    @ApiResponse(responseCode = "404", description = "Workspace not found")
    public ResponseEntity<Void> deleteWorkspace(
            @PathVariable Long workspaceId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        workspaceService.deleteWorkspace(workspaceId, userDetails.getId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{workspaceId}/members")
    @Operation(summary = "List workspace members", description = "Retrieves all members of the specified workspace")
    @ApiResponse(responseCode = "200", description = "Members retrieved successfully")
    @ApiResponse(responseCode = "403", description = "Access denied: not a workspace member")
    @ApiResponse(responseCode = "404", description = "Workspace not found")
    public ResponseEntity<List<WorkspaceMemberResponse>> getWorkspaceMembers(
            @PathVariable Long workspaceId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<WorkspaceMemberResponse> response = workspaceService.getWorkspaceMembers(workspaceId, userDetails.getId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{workspaceId}/members")
    @Operation(summary = "Add member to workspace", description = "Adds a user to the workspace with assigned role (ADMIN or OWNER only)")
    @ApiResponse(responseCode = "201", description = "Member added successfully")
    @ApiResponse(responseCode = "403", description = "Access denied: requires ADMIN or OWNER role")
    @ApiResponse(responseCode = "404", description = "Workspace or target user not found")
    @ApiResponse(responseCode = "409", description = "User is already a member")
    public ResponseEntity<WorkspaceMemberResponse> addMember(
            @PathVariable Long workspaceId,
            @Valid @RequestBody AddWorkspaceMemberRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        WorkspaceMemberResponse response = workspaceService.addMember(workspaceId, request, userDetails.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{workspaceId}/members/{userId}")
    @Operation(summary = "Update member role", description = "Updates the role of an existing workspace member (ADMIN or OWNER only)")
    @ApiResponse(responseCode = "200", description = "Member role updated successfully")
    @ApiResponse(responseCode = "400", description = "Cannot change role of workspace owner")
    @ApiResponse(responseCode = "403", description = "Access denied: requires ADMIN or OWNER role")
    @ApiResponse(responseCode = "404", description = "Workspace or member not found")
    public ResponseEntity<WorkspaceMemberResponse> updateMemberRole(
            @PathVariable Long workspaceId,
            @PathVariable Long userId,
            @Valid @RequestBody UpdateMemberRoleRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        WorkspaceMemberResponse response = workspaceService.updateMemberRole(workspaceId, userId, request, userDetails.getId());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{workspaceId}/members/{userId}")
    @Operation(summary = "Remove member from workspace", description = "Removes a member from the workspace (ADMIN or OWNER only)")
    @ApiResponse(responseCode = "204", description = "Member removed successfully")
    @ApiResponse(responseCode = "400", description = "Cannot remove workspace owner")
    @ApiResponse(responseCode = "403", description = "Access denied: requires ADMIN or OWNER role")
    @ApiResponse(responseCode = "404", description = "Workspace or member not found")
    public ResponseEntity<Void> removeMember(
            @PathVariable Long workspaceId,
            @PathVariable Long userId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        workspaceService.removeMember(workspaceId, userId, userDetails.getId());
        return ResponseEntity.noContent().build();
    }
}
