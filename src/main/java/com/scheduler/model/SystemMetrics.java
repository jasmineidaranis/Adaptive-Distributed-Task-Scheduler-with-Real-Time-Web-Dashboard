package com.scheduler.model;

public class SystemMetrics {
    private final int pendingTasks;
    private final int activeThreads;
    private final int totalThreads;
    private final int completedTasks;
    private final int failedTasks;
    private final int totalExecutions;
    
    public SystemMetrics(int pendingTasks, int activeThreads, int totalThreads,
                        int completedTasks, int failedTasks, int totalExecutions) {
        this.pendingTasks = pendingTasks;
        this.activeThreads = activeThreads;
        this.totalThreads = totalThreads;
        this.completedTasks = completedTasks;
        this.failedTasks = failedTasks;
        this.totalExecutions = totalExecutions;
    }
    
    public int getPendingTasks() { return pendingTasks; }
    public int getActiveThreads() { return activeThreads; }
    public int getTotalThreads() { return totalThreads; }
    public int getCompletedTasks() { return completedTasks; }
    public int getFailedTasks() { return failedTasks; }
    public int getTotalExecutions() { return totalExecutions; }
    
    @Override
    public String toString() {
        return String.format(
            "\n╔══════════════════════════════════════╗\n" +
            "║        SYSTEM METRICS                ║\n" +
            "╠══════════════════════════════════════╣\n" +
            "║ Pending Tasks:      %-16d║\n" +
            "║ Active Threads:     %-16d║\n" +
            "║ Thread Pool Size:   %-16d║\n" +
            "║ Completed Tasks:    %-16d║\n" +
            "║ Failed Tasks:       %-16d║\n" +
            "║ Total Executions:   %-16d║\n" +
            "╚══════════════════════════════════════╝",
            pendingTasks, activeThreads, totalThreads, 
            completedTasks, failedTasks, totalExecutions
        );
    }
}