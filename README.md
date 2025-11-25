# Adaptive-Distributed-Task-Scheduler-with-Real-Time-Web-Dashboard

A high-performance distributed task scheduling system built in Java featuring priority-based execution, dynamic thread pool scaling, persistent task history, and a real-time monitoring dashboard. Designed for enterprise workloads and optimized with Java Concurrency utilities.

⚡ Key Features

✅ Priority-Based Task Execution

Tasks are executed based on HIGH, MEDIUM, and LOW priorities.

Real-time queue management.

✅ Adaptive Thread Pool

Automatically scales thread count based on system load.

Efficient resource utilization.

✅ Real-Time Web Dashboard

Live task monitoring through an HTTP server.

Auto-refreshing metrics every second.

Color-coded statuses and execution timeline.

✅ Persistent Storage

Stores tasks and execution history using H2 Database.

Automatic schema creation on startup.

✅ Full Concurrency Support

ThreadPoolExecutor

BlockingQueue

ScheduledExecutorService

System metrics monitoring

🛠️ Setup Instructions
Prerequisites

Java 11+

Maven 3.6+


Build & Run



Compile

mvn clean package



Run Application

mvn exec:java -Dexec.mainClass="com.scheduler.Main"



Run JAR

java -jar target/task-scheduler-2.0.jar



🌐 Access the Dashboard

Once running, open:

http://localhost:8080
