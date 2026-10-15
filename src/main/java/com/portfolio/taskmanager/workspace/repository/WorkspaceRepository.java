package com.portfolio.taskmanager.workspace.repository;

import com.portfolio.taskmanager.workspace.entity.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkspaceRepository extends JpaRepository<Workspace, Long> {

    List<Workspace> findByOwnerId(Long ownerId);

    @Query("SELECT DISTINCT w FROM Workspace w " +
           "LEFT JOIN WorkspaceMember wm ON wm.workspace = w " +
           "WHERE w.owner.id = :userId OR wm.user.id = :userId")
    List<Workspace> findAllAccessibleByUserId(@Param("userId") Long userId);
}
