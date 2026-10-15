package com.portfolio.taskmanager.project.repository;

import com.portfolio.taskmanager.project.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    List<Project> findByWorkspaceId(Long workspaceId);

    Optional<Project> findByIdAndWorkspaceId(Long id, Long workspaceId);

    boolean existsByIdAndWorkspaceId(Long id, Long workspaceId);

    long countByWorkspaceId(Long workspaceId);
}
