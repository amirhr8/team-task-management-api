package com.portfolio.taskmanager.task.specification;

import com.portfolio.taskmanager.task.dto.TaskFilterCriteria;
import com.portfolio.taskmanager.task.entity.Task;
import com.portfolio.taskmanager.task.entity.TaskPriority;
import com.portfolio.taskmanager.task.entity.TaskStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class TaskSpecification {

    private TaskSpecification() {
    }

    public static Specification<Task> hasProjectId(Long projectId) {
        return (root, query, cb) ->
                projectId == null ? cb.conjunction() : cb.equal(root.get("project").get("id"), projectId);
    }

    public static Specification<Task> hasStatus(TaskStatus status) {
        return (root, query, cb) ->
                status == null ? cb.conjunction() : cb.equal(root.get("status"), status);
    }

    public static Specification<Task> hasPriority(TaskPriority priority) {
        return (root, query, cb) ->
                priority == null ? cb.conjunction() : cb.equal(root.get("priority"), priority);
    }

    public static Specification<Task> hasAssignedToId(Long assignedToId) {
        return (root, query, cb) ->
                assignedToId == null ? cb.conjunction() : cb.equal(root.get("assignedTo").get("id"), assignedToId);
    }

    public static Specification<Task> filter(Long projectId, TaskFilterCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (projectId != null) {
                predicates.add(cb.equal(root.get("project").get("id"), projectId));
            }

            if (criteria != null) {
                if (criteria.status() != null) {
                    predicates.add(cb.equal(root.get("status"), criteria.status()));
                }
                if (criteria.priority() != null) {
                    predicates.add(cb.equal(root.get("priority"), criteria.priority()));
                }
                if (criteria.assignedToId() != null) {
                    predicates.add(cb.equal(root.get("assignedTo").get("id"), criteria.assignedToId()));
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
