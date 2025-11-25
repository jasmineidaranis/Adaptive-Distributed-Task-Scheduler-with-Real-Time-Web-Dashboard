package com.scheduler.model;

public enum TaskPriority {
    LOW(1), MEDIUM(5), HIGH(10), CRITICAL(20);
    
    private final int weight;
    
    TaskPriority(int weight) {
        this.weight = weight;
    }
    
    public int getWeight() {
        return weight;
    }
}