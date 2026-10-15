package com.portfolio.taskmanager.task.service;

import com.portfolio.taskmanager.common.dto.PagedResponse;
import com.portfolio.taskmanager.common.exception.ResourceNotFoundException;
import com.portfolio.taskmanager.notification.event.TaskAssignedEvent;
import com.portfolio.taskmanager.project.entity.Project;
import com.portfolio.taskmanager.project.repository.ProjectRepository;
import com.portfolio.taskmanager.task.dto.CreateTaskRequest;
import com.portfolio.taskmanager.task.dto.TaskFilterCriteria;
import com.portfolio.taskmanager.task.dto.TaskResponse;
import com.portfolio.taskmanager.task.dto.UpdateTaskRequest;
import com.portfolio.taskmanager.task.dto.UpdateTaskStatusRequest;
import com.portfolio.taskmanager.task.entity.Task;
import com.portfolio.taskmanager.task.entity.TaskStatus;
import com.portfolio.taskmanager.task.repository.TaskRepository;
import com.portfolio.taskmanager.task.specification.TaskSpecification;
import com.portfolio.taskmanager.user.entity.User;
import com.portfolio.taskmanager.user.repository.UserRepository;
import com.portfolio.taskmanager.workspace.service.WorkspaceAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final WorkspaceAccessService workspaceAccessService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public TaskResponse createTask(Long projectId, CreateTaskRequest request, Long currentUserId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));

        Long workspaceId = project.getWorkspace().getId();
        workspaceAccessService.validateMemberAccess(workspaceId, currentUserId);

        User assignedUser = null;
        if (request.assignedToId() != null) {
            assignedUser = userRepository.findById(request.assignedToId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.assignedToId()));

            // Rule: Target assigned user must be a member of the project's workspace
            workspaceAccessService.validateMemberAccess(workspaceId, assignedUser.getId());
        }

        Task task = Task.builder()
                .title(request.title().trim())
                .description(request.description() != null ? request.description().trim() : null)
                .status(TaskStatus.TODO)
                .priority(request.priority())
                .dueDate(request.dueDate())
                .project(project)
                .assignedTo(assignedUser)
                .build();

        Task savedTask = taskRepository.save(task);

        if (assignedUser != null) {
            eventPublisher.publishEvent(new TaskAssignedEvent(
                    savedTask.getId(),
                    savedTask.getTitle(),
                    assignedUser.getId(),
                    assignedUser.getEmail(),
                    currentUserId
            ));
        }

        return TaskResponse.fromEntity(savedTask);
    }

    @Transactional(readOnly = true)
    public TaskResponse getTaskById(Long taskId, Long currentUserId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));

        workspaceAccessService.validateMemberAccess(task.getProject().getWorkspace().getId(), currentUserId);

        return TaskResponse.fromEntity(task);
    }

    @Transactional(readOnly = true)
    public PagedResponse<TaskResponse> getTasksByProject(
            Long projectId,
            TaskFilterCriteria criteria,
            Pageable pageable,
            Long currentUserId
    ) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));

        workspaceAccessService.validateMemberAccess(project.getWorkspace().getId(), currentUserId);

        Specification<Task> spec = TaskSpecification.filter(projectId, criteria);
        Page<Task> taskPage = taskRepository.findAll(spec, pageable);

        Page<TaskResponse> responsePage = taskPage.map(TaskResponse::fromEntity);
        return PagedResponse.fromPage(responsePage);
    }

    @Transactional
    public TaskResponse updateTask(Long taskId, UpdateTaskRequest request, Long currentUserId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));

        Long workspaceId = task.getProject().getWorkspace().getId();
        workspaceAccessService.validateMemberAccess(workspaceId, currentUserId);

        if (request.assignedToId() != null) {
            Long previousAssignedId = task.getAssignedTo() != null ? task.getAssignedTo().getId() : null;

            if (!request.assignedToId().equals(previousAssignedId)) {
                User newAssignee = userRepository.findById(request.assignedToId())
                        .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.assignedToId()));

                // Verify target assigned user is a member of the workspace
                workspaceAccessService.validateMemberAccess(workspaceId, newAssignee.getId());
                task.assignTo(newAssignee);

                eventPublisher.publishEvent(new TaskAssignedEvent(
                        task.getId(),
                        task.getTitle(),
                        newAssignee.getId(),
                        newAssignee.getEmail(),
                        currentUserId
                ));
            }
        } else {
            task.unassign();
        }

        task.updateDetails(
                request.title(),
                request.description() != null ? request.description().trim() : null,
                request.priority(),
                request.dueDate()
        );

        Task updatedTask = taskRepository.save(task);
        return TaskResponse.fromEntity(updatedTask);
    }

    @Transactional
    public TaskResponse updateTaskStatus(Long taskId, UpdateTaskStatusRequest request, Long currentUserId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));

        workspaceAccessService.validateMemberAccess(task.getProject().getWorkspace().getId(), currentUserId);

        // Rich domain transition validation (throws InvalidTaskStatusTransitionException if invalid)
        task.updateStatus(request.status());

        Task updatedTask = taskRepository.save(task);
        return TaskResponse.fromEntity(updatedTask);
    }

    @Transactional
    public void deleteTask(Long taskId, Long currentUserId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));

        workspaceAccessService.validateMemberAccess(task.getProject().getWorkspace().getId(), currentUserId);

        taskRepository.delete(task);
    }
}
