package com.portfolio.taskmanager.task.controller;

import com.portfolio.taskmanager.common.dto.PagedResponse;
import com.portfolio.taskmanager.security.CustomUserDetails;
import com.portfolio.taskmanager.task.dto.CreateTaskRequest;
import com.portfolio.taskmanager.task.dto.TaskFilterCriteria;
import com.portfolio.taskmanager.task.dto.TaskResponse;
import com.portfolio.taskmanager.task.dto.UpdateTaskRequest;
import com.portfolio.taskmanager.task.dto.UpdateTaskStatusRequest;
import com.portfolio.taskmanager.task.entity.TaskPriority;
import com.portfolio.taskmanager.task.entity.TaskStatus;
import com.portfolio.taskmanager.task.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Tasks", description = "Task management, assignment, workflow transitions, and dynamic filtering")
@SecurityRequirement(name = "BearerAuth")
public class TaskController {

    private final TaskService taskService;

    @PostMapping("/projects/{projectId}/tasks")
    @Operation(summary = "Create task", description = "Creates a new task in the project and notifies assignee if specified")
    @ApiResponse(responseCode = "201", description = "Task created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request payload")
    @ApiResponse(responseCode = "403", description = "Access denied: not a workspace member or assignee is not a member")
    @ApiResponse(responseCode = "404", description = "Project or assignee not found")
    public ResponseEntity<TaskResponse> createTask(
            @PathVariable Long projectId,
            @Valid @RequestBody CreateTaskRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        TaskResponse response = taskService.createTask(projectId, request, userDetails.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/projects/{projectId}/tasks")
    @Operation(
            summary = "List and filter tasks",
            description = "Dynamically filters tasks by status, priority, and assigned user with pagination and sorting"
    )
    @ApiResponse(responseCode = "200", description = "Tasks retrieved successfully")
    @ApiResponse(responseCode = "403", description = "Access denied: not a workspace member")
    @ApiResponse(responseCode = "404", description = "Project not found")
    public ResponseEntity<PagedResponse<TaskResponse>> getTasksByProject(
            @PathVariable Long projectId,
            @Parameter(description = "Filter by task status") @RequestParam(required = false) TaskStatus status,
            @Parameter(description = "Filter by task priority") @RequestParam(required = false) TaskPriority priority,
            @Parameter(description = "Filter by assigned user ID") @RequestParam(required = false) Long assignedToId,
            @ParameterObject @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        TaskFilterCriteria criteria = new TaskFilterCriteria(status, priority, assignedToId);
        PagedResponse<TaskResponse> response = taskService.getTasksByProject(projectId, criteria, pageable, userDetails.getId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/tasks/{taskId}")
    @Operation(summary = "Get task details", description = "Retrieves task details by task ID")
    @ApiResponse(responseCode = "200", description = "Task retrieved successfully")
    @ApiResponse(responseCode = "403", description = "Access denied: not a workspace member")
    @ApiResponse(responseCode = "404", description = "Task not found")
    public ResponseEntity<TaskResponse> getTaskById(
            @PathVariable Long taskId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        TaskResponse response = taskService.getTaskById(taskId, userDetails.getId());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/tasks/{taskId}")
    @Operation(summary = "Update task", description = "Updates task metadata and assignment")
    @ApiResponse(responseCode = "200", description = "Task updated successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request payload")
    @ApiResponse(responseCode = "403", description = "Access denied: not a workspace member or new assignee is not a member")
    @ApiResponse(responseCode = "404", description = "Task or assignee not found")
    public ResponseEntity<TaskResponse> updateTask(
            @PathVariable Long taskId,
            @Valid @RequestBody UpdateTaskRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        TaskResponse response = taskService.updateTask(taskId, request, userDetails.getId());
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/tasks/{taskId}/status")
    @Operation(
            summary = "Transition task status",
            description = "Validates and transitions task status according to workflow state machine rules"
    )
    @ApiResponse(responseCode = "200", description = "Task status updated successfully")
    @ApiResponse(responseCode = "400", description = "Invalid status transition (e.g. TODO to DONE directly)")
    @ApiResponse(responseCode = "403", description = "Access denied: not a workspace member")
    @ApiResponse(responseCode = "404", description = "Task not found")
    public ResponseEntity<TaskResponse> updateTaskStatus(
            @PathVariable Long taskId,
            @Valid @RequestBody UpdateTaskStatusRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        TaskResponse response = taskService.updateTaskStatus(taskId, request, userDetails.getId());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/tasks/{taskId}")
    @Operation(summary = "Delete task", description = "Deletes a task by ID")
    @ApiResponse(responseCode = "204", description = "Task deleted successfully")
    @ApiResponse(responseCode = "403", description = "Access denied: not a workspace member")
    @ApiResponse(responseCode = "404", description = "Task not found")
    public ResponseEntity<Void> deleteTask(
            @PathVariable Long taskId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        taskService.deleteTask(taskId, userDetails.getId());
        return ResponseEntity.noContent().build();
    }
}
