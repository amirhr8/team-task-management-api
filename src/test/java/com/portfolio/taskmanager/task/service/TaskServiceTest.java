package com.portfolio.taskmanager.task.service;

import com.portfolio.taskmanager.common.exception.InvalidTaskStatusTransitionException;
import com.portfolio.taskmanager.common.exception.ResourceNotFoundException;
import com.portfolio.taskmanager.notification.event.TaskAssignedEvent;
import com.portfolio.taskmanager.project.entity.Project;
import com.portfolio.taskmanager.project.repository.ProjectRepository;
import com.portfolio.taskmanager.task.dto.CreateTaskRequest;
import com.portfolio.taskmanager.task.dto.TaskResponse;
import com.portfolio.taskmanager.task.dto.UpdateTaskStatusRequest;
import com.portfolio.taskmanager.task.entity.Task;
import com.portfolio.taskmanager.task.entity.TaskPriority;
import com.portfolio.taskmanager.task.entity.TaskStatus;
import com.portfolio.taskmanager.task.repository.TaskRepository;
import com.portfolio.taskmanager.user.entity.Role;
import com.portfolio.taskmanager.user.entity.User;
import com.portfolio.taskmanager.user.repository.UserRepository;
import com.portfolio.taskmanager.workspace.entity.Workspace;
import com.portfolio.taskmanager.workspace.service.WorkspaceAccessService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private WorkspaceAccessService workspaceAccessService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private TaskService taskService;

    private User requester;
    private User assignee;
    private Workspace workspace;
    private Project project;
    private Task task;

    @BeforeEach
    void setUp() {
        requester = User.builder()
                .id(1L)
                .email("requester@portfolio.com")
                .fullName("Requester User")
                .role(Role.ROLE_USER)
                .createdAt(LocalDateTime.now())
                .build();

        assignee = User.builder()
                .id(2L)
                .email("assignee@portfolio.com")
                .fullName("Assignee User")
                .role(Role.ROLE_USER)
                .createdAt(LocalDateTime.now())
                .build();

        workspace = Workspace.builder()
                .id(100L)
                .name("Core Engineering")
                .description("Engineering workspace")
                .owner(requester)
                .createdAt(LocalDateTime.now())
                .build();

        project = Project.builder()
                .id(10L)
                .name("Backend Architecture")
                .description("Sprint 1 Backend")
                .workspace(workspace)
                .createdAt(LocalDateTime.now())
                .build();

        task = Task.builder()
                .id(1000L)
                .title("Implement Security")
                .description("Setup Spring Security 6")
                .status(TaskStatus.TODO)
                .priority(TaskPriority.HIGH)
                .dueDate(LocalDate.now().plusDays(7))
                .project(project)
                .assignedTo(assignee)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("createTask_Success: Should persist task and publish assignment event when valid")
    void createTask_Success() {
        // Arrange
        CreateTaskRequest request = new CreateTaskRequest(
                "Implement Authentication",
                "Using JJWT 0.12.x",
                TaskPriority.HIGH,
                LocalDate.now().plusDays(5),
                2L
        );

        when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
        doNothing().when(workspaceAccessService).validateMemberAccess(100L, 1L);
        when(userRepository.findById(2L)).thenReturn(Optional.of(assignee));
        doNothing().when(workspaceAccessService).validateMemberAccess(100L, 2L);
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            Task saved = invocation.getArgument(0);
            saved.setId(1000L);
            return saved;
        });

        // Act
        TaskResponse response = taskService.createTask(10L, request, 1L);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1000L);
        assertThat(response.title()).isEqualTo("Implement Authentication");
        assertThat(response.status()).isEqualTo(TaskStatus.TODO);
        assertThat(response.priority()).isEqualTo(TaskPriority.HIGH);
        assertThat(response.assignedTo()).isNotNull();
        assertThat(response.assignedTo().id()).isEqualTo(2L);
        assertThat(response.assignedTo().email()).isEqualTo("assignee@portfolio.com");

        verify(workspaceAccessService, times(1)).validateMemberAccess(100L, 1L);
        verify(workspaceAccessService, times(1)).validateMemberAccess(100L, 2L);
        verify(taskRepository, times(1)).save(any(Task.class));
        verify(eventPublisher, times(1)).publishEvent(any(TaskAssignedEvent.class));
    }

    @Test
    @DisplayName("updateTaskStatus_InvalidTransition_ThrowsException: Should reject invalid direct transition from TODO to DONE")
    void updateTaskStatus_InvalidTransition_ThrowsException() {
        // Arrange
        UpdateTaskStatusRequest request = new UpdateTaskStatusRequest(TaskStatus.DONE);

        when(taskRepository.findById(1000L)).thenReturn(Optional.of(task));
        doNothing().when(workspaceAccessService).validateMemberAccess(100L, 1L);

        // Act & Assert
        assertThatThrownBy(() -> taskService.updateTaskStatus(1000L, request, 1L))
                .isInstanceOf(InvalidTaskStatusTransitionException.class)
                .hasMessageContaining("Invalid status transition from 'TODO' to 'DONE'");

        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    @DisplayName("getTask_NotFound_ThrowsException: Should throw ResourceNotFoundException when task ID does not exist")
    void getTask_NotFound_ThrowsException() {
        // Arrange
        Long nonExistentTaskId = 9999L;
        when(taskRepository.findById(nonExistentTaskId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> taskService.getTaskById(nonExistentTaskId, 1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Task not found with id: '9999'");

        verify(workspaceAccessService, never()).validateMemberAccess(any(), any());
    }
}
