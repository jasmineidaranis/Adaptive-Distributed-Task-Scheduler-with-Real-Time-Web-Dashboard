package com.scheduler.web;

import com.scheduler.core.DistributedTaskScheduler;
import com.scheduler.model.*;
import com.sun.net.httpserver.*;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class WebServer {
    private final HttpServer server;
    private final DistributedTaskScheduler scheduler;
    
    public WebServer(DistributedTaskScheduler scheduler, int port) throws IOException {
        this.scheduler = scheduler;
        this.server = HttpServer.create(new InetSocketAddress(port), 0);
        setupRoutes();
        server.setExecutor(null);
    }
    
    private void setupRoutes() {
        server.createContext("/", this::handleDashboard);
        server.createContext("/api/metrics", this::handleMetrics);
        server.createContext("/api/history", this::handleHistory);
    }
    
    private void handleDashboard(HttpExchange exchange) throws IOException {
        String html = getDashboardHTML();
        sendResponse(exchange, 200, html, "text/html");
    }
    
    private void handleMetrics(HttpExchange exchange) throws IOException {
        SystemMetrics metrics = scheduler.getMetrics();
        String json = String.format(
            "{\"pendingTasks\":%d,\"activeThreads\":%d,\"totalThreads\":%d," +
            "\"completedTasks\":%d,\"failedTasks\":%d,\"totalExecutions\":%d}",
            metrics.getPendingTasks(), metrics.getActiveThreads(),
            metrics.getTotalThreads(), metrics.getCompletedTasks(),
            metrics.getFailedTasks(), metrics.getTotalExecutions()
        );
        sendResponse(exchange, 200, json, "application/json");
    }
    
    private void handleHistory(HttpExchange exchange) throws IOException {
        List<TaskExecutionRecord> history = scheduler.getExecutionHistory();
        StringBuilder json = new StringBuilder("[");
        
        for (int i = 0; i < history.size(); i++) {
            TaskExecutionRecord record = history.get(i);
            if (i > 0) json.append(",");
            json.append(String.format(
                "{\"taskId\":\"%s\",\"taskName\":\"%s\",\"status\":\"%s\"," +
                "\"duration\":%d,\"error\":\"%s\"}",
                record.getTaskId(), record.getTaskName(), record.getStatus(),
                record.getActualDuration() != null ? 
                    record.getActualDuration().toMillis() : 0,
                record.getErrorMessage() != null ? record.getErrorMessage() : ""
            ));
        }
        json.append("]");
        
        sendResponse(exchange, 200, json.toString(), "application/json");
    }
    
    private void sendResponse(HttpExchange exchange, int statusCode, 
                             String response, String contentType) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
    
    private String getDashboardHTML() {
        return "<!DOCTYPE html><html><head><title>Task Scheduler Dashboard</title>" +
            "<style>*{margin:0;padding:0;box-sizing:border-box}body{font-family:'Segoe UI',sans-serif;" +
            "background:linear-gradient(135deg,#667eea 0%,#764ba2 100%);min-height:100vh;padding:20px}" +
            ".container{max-width:1200px;margin:0 auto}.header{text-align:center;color:white;margin-bottom:30px}" +
            ".header h1{font-size:2.5em;margin-bottom:10px}.metrics{display:grid;grid-template-columns:repeat(auto-fit,minmax(250px,1fr));" +
            "gap:20px;margin-bottom:30px}.card{background:white;border-radius:10px;padding:20px;box-shadow:0 4px 6px rgba(0,0,0,0.1)}" +
            ".card h3{color:#667eea;margin-bottom:10px;font-size:0.9em;text-transform:uppercase;letter-spacing:1px}" +
            ".card .value{font-size:2.5em;font-weight:bold;color:#333}.history{background:white;border-radius:10px;" +
            "padding:20px;box-shadow:0 4px 6px rgba(0,0,0,0.1)}.history h2{color:#667eea;margin-bottom:15px}" +
            ".history-item{padding:10px;border-bottom:1px solid #eee;display:flex;justify-content:space-between;align-items:center}" +
            ".history-item:last-child{border-bottom:none}.status{padding:4px 12px;border-radius:20px;font-size:0.8em;font-weight:bold}" +
            ".status.COMPLETED{background:#d4edda;color:#155724}.status.FAILED{background:#f8d7da;color:#721c24}" +
            ".status.RUNNING{background:#d1ecf1;color:#0c5460}</style></head><body><div class='container'>" +
            "<div class='header'><h1>📊 Task Scheduler Dashboard</h1><p>Real-time Monitoring System</p></div>" +
            "<div class='metrics' id='metrics'></div><div class='history'><h2>Recent Executions</h2>" +
            "<div id='history'></div></div></div><script>async function fetchMetrics(){const r=await fetch('/api/metrics');" +
            "const d=await r.json();document.getElementById('metrics').innerHTML=`<div class='card'><h3>Pending Tasks</h3>" +
            "<div class='value'>${d.pendingTasks}</div></div><div class='card'><h3>Active Threads</h3>" +
            "<div class='value'>${d.activeThreads}</div></div><div class='card'><h3>Completed</h3>" +
            "<div class='value'>${d.completedTasks}</div></div><div class='card'><h3>Failed</h3>" +
            "<div class='value'>${d.failedTasks}</div></div><div class='card'><h3>Total Executions</h3>" +
            "<div class='value'>${d.totalExecutions}</div></div><div class='card'><h3>Thread Pool</h3>" +
            "<div class='value'>${d.totalThreads}</div></div>`}async function fetchHistory(){const r=await fetch('/api/history');" +
            "const d=await r.json();document.getElementById('history').innerHTML=d.map(i=>`<div class='history-item'><div><strong>" +
            "${i.taskName}</strong><br><small>${i.taskId}</small></div><div><span class='status ${i.status}'>${i.status}</span><br>" +
            "<small>${i.duration}ms</small></div></div>`).join('')}setInterval(()=>{fetchMetrics();fetchHistory()},1000);" +
            "fetchMetrics();fetchHistory()</script></body></html>";
    }
    
    public void start() {
        server.start();
        System.out.println("🌐 Web Dashboard started at http://localhost:" + 
            server.getAddress().getPort());
    }
    
    public void stop() {
        server.stop(0);
    }
}