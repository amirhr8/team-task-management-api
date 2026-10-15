package com.portfolio.taskmanager.task.entity;

import java.util.Set;

public enum TaskStatus {
    TODO,
    IN_PROGRESS,
    DONE;

    /**
     * Workflow transition rules:
     * - TODO -> IN_PROGRESS
     * - IN_PROGRESS -> TODO, DONE
     * - DONE -> IN_PROGRESS (reopening task)
     * Direct jumps such as TODO -> DONE or DONE -> TODO are disallowed.
     */
    public Set<TaskStatus> getAllowedTransitions() {
        return switch (this) {
            case TODO -> Set.of(IN_PROGRESS);
            case IN_PROGRESS -> Set.of(TODO, DONE);
            case DONE -> Set.of(IN_PROGRESS);
        };
    }

    public boolean canTransitionTo(TaskStatus target) {
        return target != null && getAllowedTransitions().contains(target);
    }
}
