package com.scheduler.model;

import java.time.Duration;

public interface Task {
    String getId();
    String getName();
    TaskPriority getPriority();
    void execute() throws Exception;
    Duration getEstimatedDuration();
}