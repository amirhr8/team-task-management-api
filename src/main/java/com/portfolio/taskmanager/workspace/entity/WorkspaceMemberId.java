package com.portfolio.taskmanager.workspace.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class WorkspaceMemberId implements Serializable {

    @Column(name = "workspace_id")
    private Long workspaceId;

    @Column(name = "user_id")
    private Long userId;
}
