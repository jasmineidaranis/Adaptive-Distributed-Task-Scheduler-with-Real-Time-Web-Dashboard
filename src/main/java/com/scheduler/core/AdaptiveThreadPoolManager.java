package com.scheduler.core;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class AdaptiveThreadPoolManager {
    private final ThreadPoolExecutor executor;
    private final AtomicInteger activeThreads = new AtomicInteger(0);
    private final int minThreads;
    private final int maxThreads;
    
    public AdaptiveThreadPoolManager(int minThreads, int maxThreads) {
        this.minThreads = minThreads;
        this.maxThreads = maxThreads;
        
        this.executor = new ThreadPoolExecutor(
            minThreads, maxThreads,
            60L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(),
            new ThreadFactory() {
                private final AtomicInteger threadNumber = new AtomicInteger(1);
                @Override
                public Thread newThread(Runnable r) {
                    Thread t = new Thread(r, "Worker-" + threadNumber.getAndIncrement());
                    t.setDaemon(false);
                    return t;
                }
            }
        );
    }
    
    public Future<?> submit(Runnable task) {
        activeThreads.incrementAndGet();
        return executor.submit(() -> {
            try {
                task.run();
            } finally {
                activeThreads.decrementAndGet();
            }
        });
    }
    
    public int getActiveThreadCount() {
        return activeThreads.get();
    }
    
    public int getPoolSize() {
        return executor.getPoolSize();
    }
    
    public void shutdown() {
        executor.shutdown();
    }
}