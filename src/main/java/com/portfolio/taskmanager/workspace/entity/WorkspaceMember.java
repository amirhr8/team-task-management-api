package com.portfolio.taskmanager.workspace.entity;

import com.portfolio.taskmanager.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(
        name = "workspace_members",
        indexes = {
                @Index(name = "idx_workspace_member_workspace_id", columnList = "workspace_id"),
                @Index(name = "idx_workspace_member_user_id", columnList = "user_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class WorkspaceMember {

    @EmbeddedId
    private WorkspaceMemberId id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("workspaceId")
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspace workspace;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("userId")
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "role_in_workspace", nullable = false, length = 20)
    private WorkspaceRole roleInWorkspace;

    @CreatedDate
    @Column(name = "joined_at", nullable = false, updatable = false)
    private LocalDateTime joinedAt;

    public WorkspaceMember(Workspace workspace, User user, WorkspaceRole roleInWorkspace) {
        this.workspace = workspace;
        this.user = user;
        this.roleInWorkspace = roleInWorkspace;
        this.id = new WorkspaceMemberId(
                workspace != null ? workspace.getId() : null,
                user != null ? user.getId() : null
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WorkspaceMember that = (WorkspaceMember) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
