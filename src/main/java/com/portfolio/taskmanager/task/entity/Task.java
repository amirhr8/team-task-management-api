package com.portfolio.taskmanager.task.entity;

import com.portfolio.taskmanager.common.exception.InvalidTaskStatusTransitionException;
import com.portfolio.taskmanager.project.entity.Project;
import com.portfolio.taskmanager.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(
        name = "tasks",
        indexes = {
                @Index(name = "idx_task_project_status", columnList = "project_id, status"),
                @Index(name = "idx_task_assigned_to", columnList = "assigned_to_id"),
                @Index(name = "idx_task_project_id", columnList = "project_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TaskStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TaskPriority priority;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to_id")
    private User assignedTo;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Domain method to transition task status according to workflow rules.
     *
     * @param newStatus Target status
     * @throws InvalidTaskStatusTransitionException if the transition is disallowed by domain rules
     */
    public void updateStatus(TaskStatus newStatus) {
        if (!this.status.canTransitionTo(newStatus)) {
            throw new InvalidTaskStatusTransitionException(
                    String.format("Invalid status transition from '%s' to '%s'. Allowed transitions: %s",
                            this.status, newStatus, this.status.getAllowedTransitions())
            );
        }
        this.status = newStatus;
    }

    public void assignTo(User user) {
        this.assignedTo = user;
    }

    public void unassign() {
        this.assignedTo = null;
    }

    public void updateDetails(String title, String description, TaskPriority priority, LocalDate dueDate) {
        if (title != null && !title.isBlank()) {
            this.title = title;
        }
        this.description = description;
        if (priority != null) {
            this.priority = priority;
        }
        this.dueDate = dueDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return id != null && Objects.equals(id, task.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
