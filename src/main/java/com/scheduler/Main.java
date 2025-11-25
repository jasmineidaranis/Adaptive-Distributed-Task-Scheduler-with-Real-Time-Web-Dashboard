package com.scheduler;

import com.scheduler.core.DistributedTaskScheduler;
import com.scheduler.model.*;
import com.scheduler.web.WebServer;
import java.time.Duration;
import java.time.Instant;

public class Main {
    
    public static void main(String[] args) throws Exception {
        System.out.println("╔════════════════════════════════════════════════╗");
        System.out.println("║  Distributed Task Scheduler System v2.0       ║");
        System.out.println("║  With Database & Web Dashboard                ║");
        System.out.println("╚════════════════════════════════════════════════╝\n");
        
        DistributedTaskScheduler scheduler = new DistributedTaskScheduler(2, 8);
        WebServer webServer = new WebServer(scheduler, 8080);
        webServer.start();
        
        // Schedule tasks
        scheduleDataProcessingTasks(scheduler);
        scheduleAnalyticsTasks(scheduler);
        scheduleMaintenanceTasks(scheduler);
        
        System.out.println("\n✓ System running. Visit http://localhost:8080 for dashboard");
        System.out.println("  Press Ctrl+C to stop\n");
        
        // Keep running
        Thread.sleep(60000);
        
        scheduler.shutdown();
        webServer.stop();
    }
    
    private static void scheduleDataProcessingTasks(DistributedTaskScheduler scheduler) {
        System.out.println("\n📊 Scheduling Data Processing Tasks...");
        
        for (int i = 1; i <= 5; i++) {
            final int taskNum = i;
            scheduler.scheduleTask(new ScheduledTask(
                "DataProcessing-" + taskNum,
                TaskPriority.HIGH,
                () -> simulateWork(500 + taskNum * 100),
                Duration.ofMillis(500),
                Instant.now().plusMillis(i * 200)
            ));
        }
    }
    
    private static void scheduleAnalyticsTasks(DistributedTaskScheduler scheduler) {
        System.out.println("\n📈 Scheduling Analytics Tasks...");
        
        scheduler.scheduleTask(new ScheduledTask(
            "UserBehaviorAnalysis",
            TaskPriority.MEDIUM,
            () -> simulateWork(1000),
            Duration.ofMillis(1000),
            Instant.now().plusMillis(300)
        ));
        
        scheduler.scheduleTask(new ScheduledTask(
            "RevenueReporting",
            TaskPriority.CRITICAL,
            () -> simulateWork(800),
            Duration.ofMillis(800),
            Instant.now().plusMillis(100)
        ));
    }
    
    private static void scheduleMaintenanceTasks(DistributedTaskScheduler scheduler) {
        System.out.println("\n🔧 Scheduling Maintenance Tasks...");
        
        scheduler.scheduleTask(new ScheduledTask(
            "DatabaseCleanup",
            TaskPriority.LOW,
            () -> simulateWork(1500),
            Duration.ofMillis(1500),
            Instant.now().plusMillis(500)
        ));
        
        scheduler.scheduleTask(new ScheduledTask(
            "BackupValidation",
            TaskPriority.MEDIUM,
            () -> {
                simulateWork(300);
                throw new RuntimeException("Validation checksum mismatch");
            },
            Duration.ofMillis(300),
            Instant.now().plusMillis(400)
        ));
    }
    
    private static void simulateWork(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}