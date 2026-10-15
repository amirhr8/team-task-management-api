package com.portfolio.taskmanager.workspace.repository;

import com.portfolio.taskmanager.workspace.entity.WorkspaceMember;
import com.portfolio.taskmanager.workspace.entity.WorkspaceMemberId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkspaceMemberRepository extends JpaRepository<WorkspaceMember, WorkspaceMemberId> {

    Optional<WorkspaceMember> findByWorkspaceIdAndUserId(Long workspaceId, Long userId);

    boolean existsByWorkspaceIdAndUserId(Long workspaceId, Long userId);

    List<WorkspaceMember> findByWorkspaceId(Long workspaceId);

    void deleteByWorkspaceIdAndUserId(Long workspaceId, Long userId);

    long countByWorkspaceId(Long workspaceId);
}
