package com.scheduler.model;

import java.time.Duration;
import java.time.Instant;

public class TaskExecutionRecord {
    private final String taskId;
    private final String taskName;
    private final TaskStatus status;
    private final Instant startTime;
    private final Instant endTime;
    private final Duration actualDuration;
    private final String errorMessage;
    
    public TaskExecutionRecord(String taskId, String taskName, TaskStatus status,
                              Instant startTime, Instant endTime, String errorMessage) {
        this.taskId = taskId;
        this.taskName = taskName;
        this.status = status;
        this.startTime = startTime;
        this.endTime = endTime;
        this.actualDuration = endTime != null ? 
            Duration.between(startTime, endTime) : null;
        this.errorMessage = errorMessage;
    }
    
    public String getTaskId() { return taskId; }
    public String getTaskName() { return taskName; }
    public TaskStatus getStatus() { return status; }
    public Instant getStartTime() { return startTime; }
    public Instant getEndTime() { return endTime; }
    public Duration getActualDuration() { return actualDuration; }
    public String getErrorMessage() { return errorMessage; }
    
    @Override
    public String toString() {
        return String.format("[%s] %s - %s (Duration: %s)%s",
            taskId, taskName, status,
            actualDuration != null ? actualDuration.toMillis() + "ms" : "N/A",
            errorMessage != null ? " Error: " + errorMessage : "");
    }
}
