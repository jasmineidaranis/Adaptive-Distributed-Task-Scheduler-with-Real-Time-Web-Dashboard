package com.scheduler.core;

import com.scheduler.database.DatabaseManager;
import com.scheduler.model.*;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

public class DistributedTaskScheduler {
    private final PriorityBlockingQueue<ScheduledTask> taskQueue;
    private final AdaptiveThreadPoolManager threadPool;
    private final ConcurrentHashMap<String, TaskStatus> taskStatusMap;
    private final CopyOnWriteArrayList<TaskExecutionRecord> executionHistory;
    private final ScheduledExecutorService schedulerService;
    private final DatabaseManager dbManager;
    private final AtomicBoolean running;
    private final AtomicInteger tasksCompleted;
    private final AtomicInteger tasksFailed;
    
    public DistributedTaskScheduler(int minThreads, int maxThreads) {
        this.taskQueue = new PriorityBlockingQueue<>();
        this.threadPool = new AdaptiveThreadPoolManager(minThreads, maxThreads);
        this.taskStatusMap = new ConcurrentHashMap<>();
        this.executionHistory = new CopyOnWriteArrayList<>();
        this.schedulerService = Executors.newScheduledThreadPool(2);
        this.dbManager = new DatabaseManager();
        this.running = new AtomicBoolean(true);
        this.tasksCompleted = new AtomicInteger(0);
        this.tasksFailed = new AtomicInteger(0);
        
        startScheduler();
    }
    
    public void scheduleTask(ScheduledTask task) {
        taskQueue.offer(task);
        taskStatusMap.put(task.getId(), TaskStatus.PENDING);
        dbManager.saveTask(task, TaskStatus.PENDING);
        System.out.println("✓ Scheduled: " + task.getName() + " [" + task.getId() + "]");
    }
    
    private void startScheduler() {
        schedulerService.scheduleAtFixedRate(() -> {
            while (running.get() && !taskQueue.isEmpty()) {
                ScheduledTask task = taskQueue.peek();
                if (task != null && Instant.now().isAfter(task.getScheduledTime())) {
                    taskQueue.poll();
                    executeTask(task);
                } else {
                    break;
                }
            }
        }, 0, 100, TimeUnit.MILLISECONDS);
    }
    
    private void executeTask(ScheduledTask task) {
        taskStatusMap.put(task.getId(), TaskStatus.RUNNING);
        dbManager.updateTaskStatus(task.getId(), TaskStatus.RUNNING);
        
        threadPool.submit(() -> {
            Instant startTime = Instant.now();
            String errorMsg = null;
            TaskStatus finalStatus = TaskStatus.COMPLETED;
            
            try {
                System.out.println("▶ Executing: " + task.getName() + " [" + task.getId() + "]");
                task.execute();
                tasksCompleted.incrementAndGet();
                System.out.println("✓ Completed: " + task.getName() + " [" + task.getId() + "]");
            } catch (Exception e) {
                finalStatus = TaskStatus.FAILED;
                errorMsg = e.getMessage();
                tasksFailed.incrementAndGet();
                System.err.println("✗ Failed: " + task.getName() + " - " + e.getMessage());
            }
            
            Instant endTime = Instant.now();
            taskStatusMap.put(task.getId(), finalStatus);
            dbManager.updateTaskStatus(task.getId(), finalStatus);
            
            TaskExecutionRecord record = new TaskExecutionRecord(
                task.getId(), task.getName(), finalStatus, startTime, endTime, errorMsg
            );
            executionHistory.add(record);
            dbManager.saveExecutionRecord(record);
        });
    }
    
    public SystemMetrics getMetrics() {
        return new SystemMetrics(
            taskQueue.size(),
            threadPool.getActiveThreadCount(),
            threadPool.getPoolSize(),
            tasksCompleted.get(),
            tasksFailed.get(),
            executionHistory.size()
        );
    }
    
    public List<TaskExecutionRecord> getExecutionHistory() {
        return new ArrayList<>(executionHistory);
    }
    
    public DatabaseManager getDatabaseManager() {
        return dbManager;
    }
    
    public void shutdown() {
        running.set(false);
        schedulerService.shutdown();
        threadPool.shutdown();
    }
}
