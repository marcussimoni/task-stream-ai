---
version: 1.0.0
last_updated: 2026-04-21
author: AI / Developer
---

# TaskStream AI Backend

A Kotlin Spring Boot 4 backend providing REST APIs for task management, scheduling, and productivity analytics with AI-powered features.

## Overview

TaskStream AI Backend serves as the data and business logic layer for the TaskStream AI platform. It provides RESTful APIs for task CRUD operations, weekly calendar scheduling, tag management, and metrics aggregation. The backend integrates with Ollama for AI features and supports both embedded H2 (development) and production database configurations.

**Target Audience:** Frontend applications and API consumers requiring task management capabilities.

## Architecture

### Tech Stack

| Layer | Technology |
|-------|------------|
| Framework | Spring Boot 4.1.0-M4 |
| Language | Kotlin 2.2.21 |
| JVM | Java 24 |
| Database | H2 (embedded) |
| Migration | Flyway |
| AI Integration | Spring AI + Ollama |
| Build Tool | Maven |

### Project Structure

```
src/main/kotlin/br/com/taskstreamai/
├── config/              # Application configuration
├── controller/          # REST API controllers
│   ├── AlarmController.kt
│   ├── ApplicationLogController.kt
│   ├── BackupDatabaseController.kt
│   ├── MetricsController.kt
│   ├── TagController.kt
│   ├── TaskController.kt
│   └── WeekCalendarController.kt
├── dto/                 # Data Transfer Objects
├── exception/           # Custom exceptions
├── mapper/              # Entity-DTO mappers
├── model/               # JPA entities
├── repository/          # Spring Data repositories
└── service/             # Business logic services

src/main/resources/
├── application.properties
├── db/migration/        # Flyway migrations
└── static/              # Frontend build output
```

## Installation

### Prerequisites

- Java 24+
- Maven 3.6+
- Ollama for AI features
- GraalVM 24+ for native compilation

### GraalVM Native Compilation - final build

Compile the Spring Boot backend to a native executable for faster startup and lower memory usage.

```bash
# Build native binary (requires GraalVM installed)
./mvnw -Pnative native:compile

# The binary will be created at:
# target/task-stream-ai
```

**Benefits:**
- **Fast startup**: Near-instant application startup
- **Lower memory footprint**: Reduced RAM usage
- **No JVM required**: Self-contained executable
- **Perfect for desktop app**: Bundled with Electron app


### Development environment configuration

This project is optimized for GraalVM Native Image. To streamline development and avoid long compilation wait times, follow these guidelines:

1. Faster Validation (AOT Mode)

Instead of waiting for a full native build, you can simulate the "Closed-World" constraints in your IDE by adding this VM argument:

```
-Dspring.aot.enabled=true;
```

Why? This enables Spring's Ahead-of-Time transformations. If your app is missing a RuntimeHintsRegistrar entry for reflection, the app will fail fast during startup in your IDE, saving you from a 5+ minute native build failure.

2. Automated Configuration (The Agent)

The following command is used to automatically generate reflection metadata.
```
-agentlib:native-image-agent=config-output-dir=src/main/resources/META-INF/native-image
```
Note: At the time of development, GraalVM 25 (Early Access) has a known regression with the agent processing Kotlin built-ins. If you encounter a List must have exactly 2 element(s) error, you may need to:

* Temporarily downgrade to GraalVM 21 (LTS) to run the agent.

* Or, manually add the missing classes to KotlinReflectionHints.kt.

Link to the issue at GitHub [#13106](https://github.com/oracle/graal/issues/13106)

### Setup

1. Clone and navigate to backend:
   ```bash
   cd backend
   ```

2. Run with Maven:
   ```bash
   ./mvnw spring-boot:run
   ```

3. Or build and run:
   ```bash
   ./mvnw clean package
   java -jar target/task-stream-ai-*.jar
   ```

Backend runs at: `http://localhost:8080`

## Usage

### API Endpoints

**Base URL:** `http://localhost:8080/api`

| Resource | Endpoint | Operations |
|----------|----------|------------|
| Tasks | `/api/tasks` | GET, POST, PUT, DELETE |
| Tags | `/api/tags` | GET, POST, PUT, DELETE |
| Week Schedule | `/api/week-schedule` | GET, POST |
| Metrics | `/api/metrics` | GET |
| Alarms | `/api/alarms` | GET, POST, DELETE |
| Application Logs | `/api/logs` | GET |
| Health | `/actuator/health` | GET |

### Database Access

**H2 Console (Development):**
- URL: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:file:~/data/h2/trackdailyapp`
- Username: `sa`
- Password: `password`

### AI Features

Requires Ollama running locally:
```bash
ollama run llama3.2
```

AI endpoints provide task suggestions and productivity insights.

## Development

### Build Commands

```bash
# Development run
./mvnw spring-boot:run

# Run tests
./mvnw test

# Build for production
./mvnw clean package

# Build with frontend included
./mvnw clean package -P frontend

# Native image (requires GraalVM)
./mvnw native:compile
```

### Code Standards

- **Language:** Kotlin with strict null safety
- **Architecture:** Layered (Controller → Service → Repository)
- **Persistence:** Spring Data JPA with Flyway migrations
- **DTOs:** MapStruct or manual mappers for entity conversion
- **Error Handling:** Global exception handler with problem details

### Profiles

- `dev`: Enhanced logging, H2 console enabled
- `prod`: Production database, se