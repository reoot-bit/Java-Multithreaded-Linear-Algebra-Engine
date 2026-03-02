# Linear Algebra Engine (LAE) - Java Multi-threading

A high-performance computational engine designed to perform linear algebra operations efficiently by exploiting parallel computing power through a custom thread pool architecture.

## 🏗️ Architecture & Design
The system is built to handle complex nested matrix operations using a task-decomposition strategy:
* [cite_start]**Custom Thread Pool:** Implementation of the `TiredExecutor` and `TiredThread` classes, managing worker threads with a unique "fatigue" and "fairness" scheduling policy[cite: 1254, 1315].
* [cite_start]**Shared Memory Management:** Safe concurrent access to `SharedMatrix` and `SharedVector` objects using `ReentrantReadWriteLock` for fine-grained synchronization[cite: 1285, 1292].
* [cite_start]**Task Scheduling:** Orchestration of a `ComputationNode` tree, breaking down operations into row/column-level tasks executed in parallel[cite: 1114, 1120].

## ✨ Key Features
* [cite_start]**Matrix Operations:** Supports Addition, Multiplication, Transpose, and Negation over real numbers [cite: 1022-1025].
* [cite_start]**Thread Safety:** Implements strict locking disciplines to prevent race conditions while maximizing throughput[cite: 1296].
* [cite_start]**Dynamic Scheduling:** Assigns tasks to the least-fatigued workers to ensure a uniform workload distribution[cite: 1320].
* [cite_start]**Robust Parsing:** Processes complex nested operations via JSON input files[cite: 1020, 1064].

## 🛠️ Technologies
* [cite_start]**Language:** Java 21[cite: 971].
* [cite_start]**Build System:** Maven (Dependency management and lifecycle control)[cite: 955].
* [cite_start]**Testing:** Comprehensive Unit Testing suite for core mathematical logic[cite: 1001].

## ⚙️ Build & Execution
### Prerequisites
* [cite_start]JDK 21 or higher[cite: 971].
* [cite_start]Apache Maven[cite: 972].

### Commands
1. **Compile:**
   ```bash
   mvn compile