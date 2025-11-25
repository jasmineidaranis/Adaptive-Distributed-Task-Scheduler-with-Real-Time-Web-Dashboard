package com.scheduler.database;

import com.scheduler.model.*;
import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:h2:./taskscheduler";
    private static final String USER = "sa";
    private static final String PASS = "";
    
    public DatabaseManager() {
        initializeDatabase();
    }
    
    private void initializeDatabase() {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             Statement stmt = conn.createStatement()) {
            
            // Create tasks table
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS tasks (" +
                "id VARCHAR(50) PRIMARY KEY," +
                "name VARCHAR(255) NOT NULL," +
                "priority VARCHAR(20) NOT NULL," +
                "status VARCHAR(20) NOT NULL," +
                "scheduled_time TIMESTAMP NOT NULL," +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)"
            );
            
            // Create execution_history table
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS execution_history (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "task_id VARCHAR(50) NOT NULL," +
                "task_name VARCHAR(255) NOT NULL," +
                "status VARCHAR(20) NOT NULL," +
                "start_time TIMESTAMP NOT NULL," +
                "end_time TIMESTAMP," +
                "duration_ms BIGINT," +
                "error_message TEXT," +
                "FOREIGN KEY (task_id) REFERENCES tasks(id))"
            );
            
            System.out.println("✓ Database initialized successfully");
            
        } catch (SQLException e) {
            System.err.println("Database initialization error: " + e.getMessage());
        }
    }
    
    public void saveTask(ScheduledTask task, TaskStatus status) {
        String sql = "INSERT INTO tasks (id, name, priority, status, scheduled_time) " +
                     "VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, task.getId());
            pstmt.setString(2, task.getName());
            pstmt.setString(3, task.getPriority().name());
            pstmt.setString(4, status.name());
            pstmt.setTimestamp(5, Timestamp.from(task.getScheduledTime()));
            
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            System.err.println("Error saving task: " + e.getMessage());
        }
    }
    
    public void updateTaskStatus(String taskId, TaskStatus status) {
        String sql = "UPDATE tasks SET status = ? WHERE id = ?";
        
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, status.name());
            pstmt.setString(2, taskId);
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            System.err.println("Error updating task status: " + e.getMessage());
        }
    }
    
    public void saveExecutionRecord(TaskExecutionRecord record) {
        String sql = "INSERT INTO execution_history " +
                     "(task_id, task_name, status, start_time, end_time, duration_ms, error_message) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, record.getTaskId());
            pstmt.setString(2, record.getTaskName());
            pstmt.setString(3, record.getStatus().name());
            pstmt.setTimestamp(4, Timestamp.from(record.getStartTime()));
            pstmt.setTimestamp(5, record.getEndTime() != null ? 
                Timestamp.from(record.getEndTime()) : null);
            pstmt.setLong(6, record.getActualDuration() != null ? 
                record.getActualDuration().toMillis() : 0);
            pstmt.setString(7, record.getErrorMessage());
            
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            System.err.println("Error saving execution record: " + e.getMessage());
        }
    }
    
    public List<TaskExecutionRecord> getExecutionHistory(int limit) {
        List<TaskExecutionRecord> history = new ArrayList<>();
        String sql = "SELECT * FROM execution_history ORDER BY start_time DESC LIMIT ?";
        
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, limit);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                TaskExecutionRecord record = new TaskExecutionRecord(
                    rs.getString("task_id"),
                    rs.getString("task_name"),
                    TaskStatus.valueOf(rs.getString("status")),
                    rs.getTimestamp("start_time").toInstant(),
                    rs.getTimestamp("end_time") != null ? 
                        rs.getTimestamp("end_time").toInstant() : null,
                    rs.getString("error_message")
                );
                history.add(record);
            }
            
        } catch (SQLException e) {
            System.err.println("Error retrieving execution history: " + e.getMessage());
        }
        
        return history;
    }
}
