package com.portfolio.taskmanager.common.exception;

public class InvalidTaskStatusTransitionException extends RuntimeException {

    public InvalidTaskStatusTransitionException(String message) {
        super(message);
    }
}
