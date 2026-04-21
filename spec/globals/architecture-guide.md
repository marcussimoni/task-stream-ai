# Architecture Guide - Task Stream AI

This document defines the architectural structure, conventions, and guardrails for the Task Stream AI application. All development must adhere to these guidelines to maintain consistency and code quality.

---

## 1. Overview

**Application Type**: Full-stack web application  
**Architecture Pattern**: Layered Architecture (Backend) + Feature-Based Modular Architecture (Frontend)

### Tech Stack Summary
| Layer | Technology | Version |
|-------|------------|---------|
| Backend | Kotlin + Spring Boot | 2.2.21 / 4.1.0-M4 |
| Frontend | Angular | 21.2.7 |
| Database | H2 (embedded) | - |
| Migrations | Flyway | - |
| ORM | Spring Data JPA | - |

---

## 2. Backend Architecture (Kotlin/Spring Boot)

### 2.1 Layer Structure

The backend follows a strict layered architecture with clear dependency direction:

```
Controller Layer (REST API)
    ↓ depends on
Service Layer (Business Logic)
    ↓ depends on
Repository Layer (Data Access)
    ↓ depends on
Model Layer (Entities)
```

**Dependency Rule**: Layers can only depend on the layer directly below them. Never skip layers (e.g., Controller should not directly access Repository).

### 2.2 Package Structure

```
src/main/kotlin/br/com/dailytrack/
├── config/              # Configuration classes
│   ├── WebConfig.kt
│   └── GlobalExceptionHandler.kt
├── controller/          # REST controllers (API endpoints)
│   ├── TaskController.kt
│   ├── TagController.kt
│   ├── HabitController.kt
│   └── ...
├── dto/                 # Data Transfer Objects
│   ├── TaskDTO.kt
│   ├── TaskRequestDTO.kt
│   └── ...
├── exception/           # Custom exceptions
│   └── ResourceNotFoundException.kt
├── mapper/              # Entity ↔ DTO converters
│   ├── TaskMapper.kt
│   └── ...
├── model/               # JPA entities
│   ├── Task.kt
│   ├── Tag.kt
│   └── ...
├── repository/          # Spring Data JPA repositories
│   ├── TaskRepository.kt
│   └── ...
└── service/             # Business logic
    ├── TaskService.kt
    └── ...
```

### 2.3 Layer Responsibilities

#### Controller Layer
- **Purpose**: Handle HTTP requests/responses
- **Rules**:
  - Use `@RestController` annotation
  - Base path: `@RequestMapping("/api/entity-name")`
  - Return `ResponseEntity<T>` for full control over status codes
  - Delegate all business logic to Services
  - Never contain business logic
  - Validate input using DTOs

**Naming Convention**: `{Entity}Controller.kt`

#### Service Layer
- **Purpose**: Implement business logic and orchestrate operations
- **Rules**:
  - Use `@Service` annotation
  - Inject Repositories via constructor
  - Handle transactions with `@Transactional`
  - Perform data validation beyond DTO constraints
  - Map between Entities and DTOs using Mappers
  - Throw domain-specific exceptions

**Naming Convention**: `{Entity}Service.kt`

#### Repository Layer
- **Purpose**: Data access abstraction
- **Rules**:
  - Extend `JpaRepository<Entity, ID>`
  - Use Spring Data JPA method naming for simple queries
  - Use `@Query` for complex JPQL/SQL
  - Return `Optional<Entity>` for single-result queries
  - Return `List<Entity>` for multi-result queries

**Naming Convention**: `{Entity}Repository.kt`

#### Mapper Layer
- **Purpose**: Convert between Entities and DTOs
- **Rules**:
  - Use pure Kotlin functions (no Spring annotations)
  - Handle null safety explicitly
  - Map all fields explicitly (avoid generic mappers)
  - Support bidirectional mapping: `toDTO()` and `toEntity()`

**Naming Convention**: `{Entity}Mapper.kt`

#### DTO Layer
- **Purpose**: Define data structures for API contracts
- **Rules**:
  - Use `data class` in Kotlin
  - Use nullable types (`?`) for optional fields
  - Use `val` (immutable) for response DTOs
  - Use `var` for request DTOs when needed
  - Separate Request and Response DTOs when structures differ

**Naming Conventions**:
- Response: `{Entity}DTO.kt`
- Request: `{Entity}RequestDTO.kt` or `Create{Entity}Request.kt`

#### Model Layer
- **Purpose**: JPA entity definitions
- **Rules**:
  - Use `data class` with `@Entity` annotation
  - Use `@Table(name = "table_name")` for explicit naming
  - Use `@Id @GeneratedValue` for primary keys
  - Use `@ManyToOne`, `@OneToMany` for relationships
  - Use `@Enumerated(EnumType.STRING)` for enums
  - Define default values for non-nullable fields

**Naming Convention**: `{Entity}.kt` (singular form)

### 2.4 Backend Development Guardrails

#### File Organization
- One public class per file
- File name matches class name
- Place file in package matching its layer

#### Import Rules
```kotlin
// Standard order:
import jakarta.persistence.*           // JPA annotations first
import org.springframework.*          // Spring framework
import java.time.*                    // Java standard library
import br.com.taskstreamai.*            // Project imports last
```

#### Exception Handling
- Use `GlobalExceptionHandler.kt` for centralized error handling
- Create custom exceptions in `exception/` package
- Map exceptions to appropriate HTTP status codes

#### API Response Pattern
```kotlin
// Success
ResponseEntity.ok(dto)
ResponseEntity.status(HttpStatus.CREATED).body(dto)

// Error (handled by GlobalExceptionHandler)
throw ResourceNotFoundException("Task not found with id: $id")
```

---

## 3. Frontend Architecture (Angular)

### 3.1 Module Structure

The frontend uses a feature-based modular architecture:

```
frontend/src/app/
├── core/                # Singleton services, models, interceptors
├── features/            # Feature modules (lazy-loaded)
└── shared/              # Shared components, pipes, directives
```

### 3.2 Core Module (`core/`)

**Purpose**: Application-wide singletons and core functionality

```
core/
├── core.module.ts       # Core module definition
├── interceptors/        # HTTP interceptors
│   └── error.interceptor.ts
├── models/              # TypeScript interfaces
│   ├── task.model.ts
│   ├── tag.model.ts
│   └── ...
├── services/            # API services (singletons)
│   ├── task.service.ts
│   ├── tag.service.ts
│   └── ...
└── index.ts             # Public API exports
```

**Rules**:
- Import `CoreModule` only in `AppModule`
- Services here are application-wide singletons
- Models define TypeScript interfaces matching backend DTOs
- HTTP interceptors for global request/response handling

### 3.3 Features Module (`features/`)

**Purpose**: Self-contained feature modules

```
features/
├── tasks/               # Task management feature
│   ├── tasks.component.ts
│   ├── tasks.component.html
│   ├── tasks.component.css
│   └── tasks.module.ts
├── tags/                # Tag management feature
├── habits/              # Habit tracking feature
├── calendar/            # Calendar view feature
├── metrics/             # Dashboard feature
└── ...
```

**Rules**:
- Each feature is a self-contained unit
- Feature components use standalone: true (Angular 21+)
- Feature-specific services can be provided here
- Lazy loading encouraged for larger features

### 3.4 Shared Module (`shared/`)

**Purpose**: Reusable components and utilities used across features

```
shared/
├── components/          # Reusable UI components
│   ├── toast/           # Toast notification system
│   ├── nav/             # Navigation component
│   ├── header/          # Header component
│   └── confirm-dialog/  # Confirmation dialogs
├── models/              # Shared models
├── services/            # Shared services (ToastService, etc.)
├── shared.module.ts     # Shared module definition
└── index.ts             # Public API exports
```

**Rules**:
- Components here must be truly reusable
- No business logic in shared components
- Use `SharedModule` imports where needed

### 3.5 Component Structure

Each component follows this file structure:
```
{feature-name}/
├── {feature-name}.component.ts      # Component logic
├── {feature-name}.component.html   # Template
├── {feature-name}.component.css    # Scoped styles
└── {feature-name}.module.ts        # Module (if needed)
```

#### Component Class Pattern
```typescript
@Component({
  selector: 'app-{kebab-case-name}',
  standalone: true,  // Angular 21+ pattern
  imports: [CommonModule, ReactiveFormsModule, ...],
  templateUrl: './{name}.component.html',
  styleUrls: ['./{name}.component.css']
})
export class {PascalCaseName}Component implements OnInit {
  // Use signals for state management (Angular 21+)
  private tasks = signal<Task[]>([]);
  readonly tasksList = this.tasks.asReadonly();
  
  // Use inject() for dependency injection
  private taskService = inject(TaskService);
  private toastService = inject(ToastService);
  
  ngOnInit(): void {
    this.loadTasks();
  }
  
  private loadTasks(): void {
    this.taskService.getAll().subscribe({
      next: (tasks) => this.tasks.set(tasks),
      error: (err) => this.toastService.error(this.extractErrorMessage(err))
    });
  }
  
  private extractErrorMessage(error: any): string {
    return error.error?.message || error.error?.error || error.message || 'An error occurred';
  }
}
```

### 3.6 Service Pattern

#### API Service Structure
```typescript
@Injectable({ providedIn: 'root' })
export class TaskService {
  private http = inject(HttpClient);
  private apiUrl = '/api/tasks';
  
  getAll(): Observable<Task[]> {
    return this.http.get<Task[]>(this.apiUrl);
  }
  
  getById(id: number): Observable<Task> {
    return this.http.get<Task>(`${this.apiUrl}/${id}`);
  }
  
  create(task: TaskRequest): Observable<Task> {
    return this.http.post<Task>(this.apiUrl, task);
  }
  
  update(id: number, task: TaskRequest): Observable<Task> {
    return this.http.put<Task>(`${this.apiUrl}/${id}`, task);
  }
  
  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
```

### 3.7 Model Definitions

#### TypeScript Interface Pattern
```typescript
// Core model matching backend DTO
export interface Task {
  id: number;
  name: string;
  description?: string;
  priority: Priority;
  completed: boolean;
  tag: Tag;
  startDate?: string;  // ISO date string
  endDate?: string;
}

export enum Priority {
  LOW = 'LOW',
  MEDIUM = 'MEDIUM',
  HIGH = 'HIGH',
  CRITICAL = 'CRITICAL'
}

export interface TaskRequest {
  name: string;
  description?: string;
  priority: Priority;
  tagId: number;
  startDate?: string;
  endDate?: string;
}
```

### 3.8 Frontend Development Guardrails

#### File Naming Conventions
| Type | Pattern | Example |
|------|---------|---------|
| Component | `{name}.component.{ext}` | `tasks.component.ts` |
| Service | `{name}.service.ts` | `task.service.ts` |
| Model | `{name}.model.ts` | `task.model.ts` |
| Module | `{name}.module.ts` | `tasks.module.ts` |
| Interceptor | `{name}.interceptor.ts` | `error.interceptor.ts` |

#### Import Order
```typescript
// 1. Angular core/framework imports
import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';

// 2. Third-party imports
import { Observable } from 'rxjs';

// 3. Application imports (alias paths)
import { TaskService } from '../../core/services/task.service';
import { ToastService } from '../../shared/services/toast.service';
```

#### State Management
- Use Angular Signals (`signal()`, `computed()`, `effect()`) for component state
- Use RxJS Observables for HTTP and async operations
- Use `async` pipe in templates for observable streams
- Avoid `any` type - define interfaces for all data structures

#### Error Handling Pattern
```typescript
// In component
this.service.operation().subscribe({
  next: (result) => this.handleSuccess(result),
  error: (err) => {
    const message = this.extractErrorMessage(err);
    this.toastService.error(message);
  }
});

private extractErrorMessage(error: any): string {
  return error.error?.message 
    || error.error?.error 
    || error.message 
    || 'Operation failed';
}
```

---

## 4. Cross-Cutting Concerns

### 4.1 Database Conventions

#### Table Naming
- Use lowercase with underscores: `task_types`, `habit_entries`
- Singular form for entity tables: `task`, `tag`
- Migration files: `V{version}__{description}.sql`

#### Entity Relationships
- Use `@JoinColumn` for foreign key naming
- Use `FetchType.LAZY` for collections
- Use `CascadeType.ALL` for owned relationships

### 4.2 API Conventions

#### URL Patterns
| Operation | Pattern | Example |
|-----------|---------|---------|
| List | `GET /api/{entities}` | `GET /api/tasks` |
| Get One | `GET /api/{entities}/{id}` | `GET /api/tasks/1` |
| Create | `POST /api/{entities}` | `POST /api/tasks` |
| Update | `PUT /api/{entities}/{id}` | `PUT /api/tasks/1` |
| Delete | `DELETE /api/{entities}/{id}` | `DELETE /api/tasks/1` |

#### Response Codes
| Code | Usage |
|------|-------|
| 200 OK | Successful GET, PUT |
| 201 Created | Successful POST |
| 204 No Content | Successful DELETE |
| 400 Bad Request | Validation error |
| 404 Not Found | Resource doesn't exist |
| 500 Internal Server Error | Unexpected error |

### 4.3 Toast Notification Usage

All user-facing operations should provide feedback via toast notifications:

```typescript
// Success case
this.toastService.success('Task created successfully');

// Error case
this.toastService.error('Failed to create task', 'Error');

// Warning case
this.toastService.warning('Some habits could not be updated');
```

---

## 5. File Templates

### 5.1 New Backend Entity Template

When creating a new entity, create these files in order:

1. **Model** (`model/{Entity}.kt`)
2. **DTO** (`dto/{Entity}DTO.kt`, `dto/Create{Entity}Request.kt`)
3. **Repository** (`repository/{Entity}Repository.kt`)
4. **Mapper** (`mapper/{Entity}Mapper.kt`)
5. **Service** (`service/{Entity}Service.kt`)
6. **Controller** (`controller/{Entity}Controller.kt`)

### 5.2 New Frontend Feature Template

When creating a new feature:

1. **Model** (`core/models/{entity}.model.ts`)
2. **Service** (`core/services/{entity}.service.ts`)
3. **Component** (`features/{entity}/{entity}.component.{ts,html,css}`)
4. **Add Route** (`app-routing.module.ts`)
5. **Add Nav Link** (`shared/components/nav/`)

---

## 6. Development Checklist

Before submitting code, verify:

- [ ] File follows naming conventions
- [ ] Code is placed in correct layer/package
- [ ] No layer violations (Controller → Service → Repository)
- [ ] DTOs match between frontend and backend
- [ ] Error handling includes toast notifications
- [ ] No `any` types in TypeScript
- [ ] Signals used for component state (not manual change detection)
- [ ] Backend returns appropriate HTTP status codes
- [ ] Kotlin code uses null-safety properly (`?`, `?:`, `!!`)
- [ ] Database migration added if schema changed

---

## 7. Reference

### Backend Controllers
- `AchievementController.kt`
- `BackupDatabaseController.kt`
- `CalendarController.kt`
- `HabitController.kt`
- `MetricsController.kt`
- `TagController.kt`
- `TaskController.kt`
- `WeekCalendarController.kt`

### Backend Services
- `AchievementService.kt`
- `CalendarService.kt`
- `DatabaseBackupService.kt`
- `HabitEntryService.kt`
- `HabitService.kt`
- `MetricsService.kt`
- `StreakService.kt`
- `SummarizeArticleService.kt`
- `TagService.kt`
- `TaskService.kt`
- `WeekScheduleService.kt`

### Frontend Features
- `achievements/` - Achievement management
- `admin/` - Admin panel (database backup)
- `calendar/` - Habit calendar tracking
- `habits/` - Habit CRUD
- `metrics/` - Dashboard with charts
- `monthly-overview/` - Monthly task view
- `planned-habits/` - Batch habit completion
- `tags/` - Tag management
- `tasks/` - Task management with priority
- `weekly-calendar/` - Week schedule

### Shared Components
- `toast/` - Toast notification system
- `nav/` - Navigation component
- `header/` - Header component
- `confirm-dialog/` - Confirmation dialogs
