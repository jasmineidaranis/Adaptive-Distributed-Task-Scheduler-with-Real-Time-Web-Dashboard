package com.scheduler.model;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

public class ScheduledTask implements Task, Comparable<ScheduledTask> {
    private final String id;
    private final String name;
    private final TaskPriority priority;
    private final Runnable action;
    private final Duration estimatedDuration;
    private final Instant scheduledTime;
    
    public ScheduledTask(String name, TaskPriority priority, Runnable action, 
                        Duration estimatedDuration, Instant scheduledTime) {
        this.id = UUID.randomUUID().toString().substring(0, 8);
        this.name = name;
        this.priority = priority;
        this.action = action;
        this.estimatedDuration = estimatedDuration;
        this.scheduledTime = scheduledTime;
    }
    
    @Override
    public String getId() {
        return id;
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public TaskPriority getPriority() {
        return priority;
    }
    
    @Override
    public void execute() throws Exception {
        action.run();
    }
    
    @Override
    public Duration getEstimatedDuration() {
        return estimatedDuration;
    }
    
    public Instant getScheduledTime() {
        return scheduledTime;
    }
    
    @Override
    public int compareTo(ScheduledTask other) {
        int priorityCompare = Integer.compare(
            other.priority.getWeight(), 
            this.priority.getWeight()
        );
        if (priorityCompare != 0) return priorityCompare;
        return this.scheduledTime.compareTo(other.scheduledTime);
    }
}
