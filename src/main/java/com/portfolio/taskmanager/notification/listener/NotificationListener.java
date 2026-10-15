package com.portfolio.taskmanager.notification.listener;

import com.portfolio.taskmanager.notification.event.TaskAssignedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
public class NotificationListener {

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onTaskAssigned(TaskAssignedEvent event) {
        log.info(
                "Decoupled Notification: Task '{}' (ID: {}) successfully assigned to user '{}' (ID: {}) by user ID {}",
                event.taskTitle(),
                event.taskId(),
                event.assignedUserEmail(),
                event.assignedUserId(),
                event.assignedByUserId()
        );
        // Integration hook: Send real-time WebSocket alert, push notification, or email here
    }
}
