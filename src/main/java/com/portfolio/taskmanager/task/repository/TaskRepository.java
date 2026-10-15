package com.portfolio.taskmanager.task.repository;

import com.portfolio.taskmanager.task.entity.Task;
import com.portfolio.taskmanager.task.entity.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {

    List<Task> findByProjectId(Long projectId);

    Optional<Task> findByIdAndProjectId(Long id, Long projectId);

    boolean existsByIdAndProjectId(Long id, Long projectId);

    long countByProjectId(Long projectId);

    long countByProjectIdAndStatus(Long projectId, TaskStatus status);
}
