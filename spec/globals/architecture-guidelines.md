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

---

## 8. Controller Test Specification

### 8.1 Overview
All controller tests must follow Spring Boot 4 and Mockito 5 patterns. This specification ensures consistency and maintainability across all controller tests.

### 8.2 Required Test Dependencies
```kotlin
// Spring Boot 4 Testing
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.put
import org.springframework.test.web.servlet.delete
import org.springframework.http.MediaType

// Jackson 3
import tools.jackson.databind.json.JsonMapper

// JUnit 5
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach

// Mockito 5 Core API
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
```

### 8.3 Test Class Structure
```kotlin
@WebMvcTest(ControllerClass::class)
class ControllerClassTest {
    
    @MockitoBean
    private lateinit var serviceService: ServiceClass
    
    private lateinit var mockMvc: MockMvc
    private lateinit var jsonMapper: JsonMapper
    
    @BeforeEach
    fun setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(ControllerClass(serviceService)).build()
        jsonMapper = JsonMapper.builder().findAndAddModules().build()
    }
    
    // Test methods here
}
```

### 8.4 Test Patterns

#### 8.4.1 Happy Path Tests
```kotlin
@Test
fun `should create entity successfully`() {
    // Given
    val requestDTO = CreateEntityRequestDTO(
        name = "Test Entity",
        description = "Test Description"
    )
    val expectedDTO = EntityDTO(
        id = 1L,
        name = "Test Entity",
        description = "Test Description",
        createdAt = LocalDateTime.now(),
        updatedAt = LocalDateTime.now()
    )
    
    Mockito.doReturn(expectedDTO).`when`(serviceService).createEntity(requestDTO)

    // When & Then
    mockMvc.post("/api/entities") {
        contentType = MediaType.APPLICATION_JSON
        content = jsonMapper.writeValueAsString(requestDTO)
    }.andExpect {
        status().isCreated()
        content().contentType(MediaType.APPLICATION_JSON)
        jsonPath("$.id").value(1)
        jsonPath("$.name").value("Test Entity")
    }
}
```

#### 8.4.2 Error Handling Tests
```kotlin
@Test
fun `should handle creation service errors gracefully`() {
    // Given
    val requestDTO = CreateEntityRequestDTO(
        name = "Error Entity",
        description = "This will cause an error"
    )
    
    Mockito.doThrow(RuntimeException("Creation failed")).`when`(serviceService).createEntity(requestDTO)

    // When & Then
    mockMvc.post("/api/entities") {
        contentType = MediaType.APPLICATION_JSON
        content = jsonMapper.writeValueAsString(requestDTO)
    }.andExpect {
        status().is5xxServerError()
    }
}
```

#### 8.4.3 Validation Tests
```kotlin
@Test
fun `should return 400 when input is empty`() {
    // Given
    val requestDTO = CreateEntityRequestDTO(
        name = "",
        description = "Empty name test"
    )

    Mockito.doReturn(emptyList<EntityDTO>()).`when`(serviceService).createEntity(requestDTO)

    // When & Then
    mockMvc.post("/api/entities") {
        contentType = MediaType.APPLICATION_JSON
        content = jsonMapper.writeValueAsString(requestDTO)
    }.andExpect {
        status().isOk()
        content().contentType(MediaType.APPLICATION_JSON)
        jsonPath("$.length()").value(0)
    }
}
```

### 8.5 Mockito 5 Usage Guidelines

#### 8.5.1 Use `doReturn()` and `doThrow()` instead of `when()`
```kotlin
// ✅ CORRECT
Mockito.doReturn(expectedDTO).`when`(serviceService).createEntity(requestDTO)
Mockito.doThrow(RuntimeException("Error")).`when`(serviceService).createEntity(requestDTO)

// ❌ AVOID (causes issues with any() matchers)
Mockito.`when`(serviceService.createEntity(any())).thenReturn(expectedDTO)
```

#### 8.5.2 Use Direct Mock Objects Instead of `any()`
```kotlin
// ✅ CORRECT
Mockito.doReturn(expectedDTO).`when`(serviceService).createEntity(requestDTO)

// ❌ AVOID (causes NullPointer issues)
Mockito.doReturn(expectedDTO).`when`(serviceService).createEntity(any(CreateEntityRequestDTO::class.java))
```

#### 8.5.3 Void Method Stubbing
```kotlin
// ✅ CORRECT
Mockito.doNothing().`when`(serviceService).deleteEntity(1L)

// ❌ AVOID
Mockito.`when`(serviceService.deleteEntity(1L)).thenAnswer(Unit)
```

### 8.6 MockMvc Assertion Patterns

#### 8.6.1 Status Assertions
```kotlin
mockMvc.post("/api/entities") {
    // request setup
}.andExpect {
    status().isOk()           // 200
    status().isCreated()      // 201
    status().isNoContent()    // 204
    status().isBadRequest()   // 400
    status().isNotFound()     // 404
    status().is5xxServerError() // 500
}
```

#### 8.6.2 Content Assertions
```kotlin
mockMvc.get("/api/entities").andExpect {
    content().contentType(MediaType.APPLICATION_JSON)
    jsonPath("$.length()").value(2)
    jsonPath("$[0].name").value("Test Entity")
    jsonPath("$[0].id").value(1)
    content().string("")  // For empty responses
}
```

### 8.7 DTO Construction Guidelines

#### 8.7.1 Include All Required Fields
```kotlin
val entityDTO = EntityDTO(
    id = 1L,
    name = "Test Entity",
    description = "Test Description",
    currentValue = 0,
    startDate = LocalDate.now(),
    endDateInterval = 1,
    endDate = null,
    completed = false,
    customEndDateSelected = false,
    priority = Priority.MEDIUM,
    tagId = 1L,
    link = null,
    summary = null,
    createdAt = LocalDateTime.now(),
    updatedAt = LocalDateTime.now()
)
```

#### 8.7.2 Use Proper Date/Time Types
```kotlin
// ✅ CORRECT
createdAt = LocalDateTime.now()
updatedAt = LocalDateTime.now()
startDate = LocalDate.now()

// ❌ AVOID (unless specifically required)
createdAt = "2024-01-01T00:00:00"
```

### 8.8 Test Naming Convention

Use descriptive test names with backticks:
```kotlin
@Test
fun `should create entity successfully`() { }

@Test
fun `should handle creation service errors gracefully`() { }

@Test
fun `should return 400 when input is empty`() { }

@Test
fun `should verify correct content type for all endpoints`() { }
```

### 8.9 Common Test Scenarios to Cover

1. **CRUD Operations**: Create, Read, Update, Delete
2. **Validation**: Empty input, invalid data
3. **Error Handling**: Service exceptions, database errors
4. **Content Type Verification**: Ensure JSON responses
5. **Status Code Verification**: Correct HTTP status codes

### 8.10 Test File Structure

```
src/test/kotlin/br/com/taskstreamai/controller/
├── ControllerClassTest.kt
└── ...
```

### 8.11 Required Test Execution

Before completing any controller implementation or refactoring:
1. Run `./mvnw test` to ensure all tests pass
2. Verify test coverage for new endpoints
3. Ensure no compilation errors related to Mockito usage
4. Confirm all tests follow the patterns above

### 8.12 Testing Best Practices

#### 8.12.1 Test Organization
- Group related tests together
- Use descriptive test method names
- Follow Given-When-Then pattern in test structure

#### 8.12.2 Mock Management
- Use `@MockitoBean` for Spring-managed beans
- Reset mocks between tests if needed
- Avoid mocking framework classes

#### 8.12.3 Assertion Strategy
- Test both success and failure scenarios
- Verify HTTP status codes and response content
- Test edge cases and boundary conditions

#### 8.12.4 Performance Considerations
- Keep tests fast and focused
- Avoid unnecessary database interactions
- Use MockMvc for controller layer testing only

---

## 9. Service Layer Test Specification

### 9.1 Overview
All service tests must follow Spring Boot 4 and Mockito 5 patterns. This specification ensures consistency and maintainability across all service layer unit tests.

### 9.2 Required Test Dependencies
```kotlin
// Spring Boot 4 Testing
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertThrows

// Mockito 5 Core API
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.ArgumentMatchers
import java.util.*

// Project imports
import br.com.taskstreamai.dto.*
import br.com.taskstreamai.exception.*
import br.com.taskstreamai.mapper.*
import br.com.taskstreamai.model.*
import br.com.taskstreamai.repository.*
import br.com.taskstreamai.service.*
```

### 9.3 Test Class Structure
```kotlin
class ServiceClassTest {
    
    @Mock
    private lateinit var repository: RepositoryClass
    @Mock
    private lateinit var dependencyService: DependencyService
    @Mock
    private lateinit var mapper: MapperClass
    
    private lateinit var service: ServiceClass
    
    @BeforeEach
    fun setup() {
        repository = Mockito.mock(RepositoryClass::class.java)
        dependencyService = Mockito.mock(DependencyService::class.java)
        mapper = Mockito.mock(MapperClass::class.java)
        
        service = ServiceClass(repository, dependencyService, mapper)
    }
    
    // Test methods here
}
```

### 9.4 Test Patterns

#### 9.4.1 Happy Path Tests
```kotlin
@Test
fun `should create entity successfully`() {
    // Given
    val requestDTO = CreateEntityRequestDTO(
        name = "Test Entity",
        description = "Test Description"
    )
    val entity = Entity(
        id = 1L,
        name = "Test Entity",
        description = "Test Description",
        tag = testTag,
        createdAt = LocalDateTime.now(),
        updatedAt = LocalDateTime.now()
    )
    val expectedDTO = EntityDTO(
        id = 1L,
        name = "Test Entity",
        description = "Test Description",
        tag = TagDTO(id = 1L, name = "Work", description = "Work tasks", color = "#FF0000", createdAt = LocalDateTime.now(), updatedAt = LocalDateTime.now()),
        createdAt = LocalDateTime.now(),
        updatedAt = LocalDateTime.now()
    )
    
    Mockito.doReturn(Optional.of(testTag)).`when`(repository).findById(1L)
    Mockito.doReturn(entity).`when`(repository).save(org.mockito.ArgumentMatchers.any(Entity::class.java))
    Mockito.doReturn(expectedDTO).`when`(mapper).toDTO(entity)

    // When
    val result = service.createEntity(requestDTO)

    // Then
    assert(result.id == 1L)
    assert(result.name == "Test Entity")
    assert(result.description == "Test Description")
}
```

#### 9.4.2 Exception Handling Tests
```kotlin
@Test
fun `should throw exception when entity not found`() {
    // Given
    Mockito.doReturn(Optional.empty<Entity>()).`when`(repository).findById(999L)

    // When & Then
    try {
        service.getEntityById(999L)
        assert(false) { "Should have thrown ResourceNotFoundException" }
    } catch (e: ResourceNotFoundException) {
        // Expected exception
    }
}
```

#### 9.4.3 Edge Case Tests
```kotlin
@Test
fun `should handle empty list gracefully`() {
    // Given
    Mockito.doReturn(emptyList<Entity>()).`when`(repository).findAll()

    // When
    val result = service.getAllEntities()

    // Then
    assert(result.isEmpty())
}

@Test
fun `should handle null input gracefully`() {
    // Given
    val emptyInputDTO = CreateEntityRequestDTO(input = "")
    Mockito.doReturn(testTag).`when`(dependencyService).getTagById(1L)

    // When
    val result = service.processEntity(emptyInputDTO)

    // Then
    assert(result != null)
    assert(result!!.isEmpty())
}
```

### 9.5 Mockito 5 Usage Guidelines

#### 9.5.1 Use `doReturn()` and `doThrow()` instead of `when()`
```kotlin
// ✅ CORRECT
Mockito.doReturn(expectedDTO).`when`(repository).findById(1L)
Mockito.doThrow(RuntimeException("Database error")).`when`(repository).save(org.mockito.ArgumentMatchers.any())

// ❌ AVOID (causes issues with any() matchers)
Mockito.`when`(repository.findById(any())).thenReturn(Optional.of(entity))
```

#### 9.5.2 Use Explicit Type Parameters for Generic Methods
```kotlin
// ✅ CORRECT
Mockito.doReturn(Optional.empty<Entity>()).`when`(repository).findById(999L)
Mockito.doReturn(Optional.empty<Tag>()).`when`(tagRepository).findById(999L)

// ❌ AVOID (causes type inference issues)
Mockito.doReturn(Optional.empty()).`when`(repository).findById(999L)
```

#### 9.5.3 Use `ArgumentMatchers.any()` with Explicit Types
```kotlin
// ✅ CORRECT
Mockito.doReturn(entity).`when`(repository).save(org.mockito.ArgumentMatchers.any(Entity::class.java))
Mockito.doReturn(emptyList<Entity>()).`when`(repository).findAllEntities(org.mockito.ArgumentMatchers.any(String::class.java))

// ❌ AVOID (causes type inference issues)
Mockito.doReturn(entity).`when`(repository).save(org.mockito.ArgumentMatchers.any())
```

#### 9.5.4 Void Method Stubbing
```kotlin
// ✅ CORRECT
Mockito.doNothing().`when`(repository).deleteById(1L)

// ❌ AVOID
Mockito.`when`(repository.deleteById(1L)).thenAnswer(Unit)
```

### 9.6 Exception Testing Patterns

#### 9.6.1 Use Try-Catch Instead of assertThrows
```kotlin
// ✅ CORRECT
@Test
fun `should throw exception when getting non-existent entity`() {
    // Given
    Mockito.doReturn(Optional.empty<Entity>()).`when`(repository).findById(999L)

    // When & Then
    try {
        service.getEntityById(999L)
        assert(false) { "Should have thrown ResourceNotFoundException" }
    } catch (e: ResourceNotFoundException) {
        // Expected exception
    }
}

// ❌ AVOID (causes type inference issues in some cases)
@Test
fun `should throw exception when getting non-existent entity`() {
    // Given
    Mockito.doReturn(Optional.empty<Entity>()).`when`(repository).findById(999L)

    // When & Then
    assertThrows<ResourceNotFoundException> {
        service.getEntityById(999L)
    }
}
```

### 9.7 DTO and Entity Construction Guidelines

#### 9.7.1 Include All Required Fields
```kotlin
val testTag = Tag(
    id = 1L,
    name = "Work",
    description = "Work tasks",
    color = "#FF0000",
    createdAt = LocalDateTime.now(),
    updatedAt = LocalDateTime.now()
)

val entity = Entity(
    id = 1L,
    name = "Test Entity",
    description = "Test Description",
    currentValue = 0,
    startDate = LocalDate.now(),
    endDateInterval = 1,
    endDate = null,
    completed = false,
    customEndDateSelected = false,
    priority = Priority.MEDIUM,
    tag = testTag,
    link = null,
    summary = null,
    createdAt = LocalDateTime.now(),
    updatedAt = LocalDateTime.now()
)
```

#### 9.7.2 Use Proper Date/Time Types
```kotlin
// ✅ CORRECT
createdAt = LocalDateTime.now()
updatedAt = LocalDateTime.now()
startDate = LocalDate.now()

// ❌ AVOID (unless specifically required)
createdAt = "2024-01-01T00:00:00"
```

### 9.8 Test Naming Convention

Use descriptive test names with backticks:
```kotlin
@Test
fun `should create entity successfully`() { }

@Test
fun `should throw exception when entity not found`() { }

@Test
fun `should handle empty list gracefully`() { }

@Test
fun `should update entity successfully`() { }

@Test
fun `should delete entity successfully`() { }
```

### 9.9 Common Test Scenarios to Cover

1. **CRUD Operations**: Create, Read, Update, Delete
2. **Exception Handling**: Resource not found, validation errors
3. **Edge Cases**: Empty lists, null inputs, invalid data
4. **Business Logic**: Complex calculations, conditional logic
5. **Integration**: Service-to-service communication

### 9.10 Test File Structure

```
src/test/kotlin/br/com/taskstreamai/service/
├── ServiceClassTest.kt
├── AnotherServiceTest.kt
└── ...
```

### 9.11 Required Test Execution

Before completing any service implementation or refactoring:
1. Run `./mvnw test -Dtest="*ServiceTest"` to ensure all service tests pass
2. Verify test coverage for new service methods
3. Ensure no compilation errors related to Mockito usage
4. Confirm all tests follow the patterns above

### 9.12 Testing Best Practices

#### 9.12.1 Test Organization
- Group related tests together
- Use descriptive test method names
- Follow Given-When-Then pattern in test structure
- Keep tests focused on single responsibility

#### 9.12.2 Mock Management
- Use `@Mock` annotations for dependency injection
- Create fresh mocks in `@BeforeEach` setup
- Avoid mocking framework classes
- Use explicit mock objects instead of `any()` matchers

#### 9.12.3 Assertion Strategy
- Test both success and failure scenarios
- Verify business logic outcomes
- Test edge cases and boundary conditions
- Use meaningful assertions with clear messages

#### 9.12.4 Performance Considerations
- Keep tests fast and focused
- Avoid unnecessary database interactions
- Use pure unit tests for service layer
- Mock all external dependencies

#### 9.12.5 Complex Service Integration
For services with complex external dependencies (like AI services):
- Focus on basic functionality testing
- Simplify tests to avoid complex mocking issues
- Test error handling and edge cases
- Consider integration tests for complex scenarios

### 9.13 Example Complete Service Test

```kotlin
package br.com.taskstreamai.service

import br.com.taskstreamai.dto.EntityDTO
import br.com.taskstreamai.dto.CreateEntityRequestDTO
import br.com.taskstreamai.exception.ResourceNotFoundException
import br.com.taskstreamai.mapper.EntityMapper
import br.com.taskstreamai.model.Entity
import br.com.taskstreamai.model.Tag
import br.com.taskstreamai.repository.EntityRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.Mockito
import java.time.LocalDateTime
import java.util.*

class EntityServiceTest {

    @Mock
    private lateinit var repository: EntityRepository
    @Mock
    private lateinit var mapper: EntityMapper
    
    private lateinit var service: EntityService
    private lateinit var testTag: Tag
    private lateinit var testEntity: Entity

    @BeforeEach
    fun setup() {
        repository = Mockito.mock(EntityRepository::class.java)
        mapper = Mockito.mock(EntityMapper::class.java)
        
        service = EntityService(repository, mapper)
        
        testTag = Tag(
            id = 1L,
            name = "Work",
            description = "Work tasks",
            color = "#FF0000",
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        
        testEntity = Entity(
            id = 1L,
            name = "Test Entity",
            description = "Test Description",
            tag = testTag,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
    }

    @Test
    fun `should get entity by id successfully`() {
        // Given
        val expectedDTO = EntityDTO(
            id = 1L,
            name = "Test Entity",
            description = "Test Description",
            tag = TagDTO(id = 1L, name = "Work", description = "Work tasks", color = "#FF0000", createdAt = LocalDateTime.now(), updatedAt = LocalDateTime.now()),
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        
        Mockito.doReturn(Optional.of(testEntity)).`when`(repository).findById(1L)
        Mockito.doReturn(expectedDTO).`when`(mapper).toDTO(testEntity)

        // When
        val result = service.getEntityById(1L)

        // Then
        assert(result.id == 1L)
        assert(result.name == "Test Entity")
        assert(result.description == "Test Description")
    }

    @Test
    fun `should throw exception when entity not found`() {
        // Given
        Mockito.doReturn(Optional.empty<Entity>()).`when`(repository).findById(999L)

        // When & Then
        try {
            service.getEntityById(999L)
            assert(false) { "Should have thrown ResourceNotFoundException" }
        } catch (e: ResourceNotFoundException) {
            // Expected exception
        }
    }

    @Test
    fun `should get all entities successfully`() {
        // Given
        val entities = listOf(testEntity)
        val expectedDTOs = listOf(EntityDTO(
            id = 1L,
            name = "Test Entity",
            description = "Test Description",
            tag = TagDTO(id = 1L, name = "Work", description = "Work tasks", color = "#FF0000", createdAt = LocalDateTime.now(), updatedAt = LocalDateTime.now()),
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        ))
        
        Mockito.doReturn(entities).`when`(repository).findAll()
        Mockito.doReturn(expectedDTOs).`when`(mapper).toDTOList(entities)

        // When
        val result = service.getAllEntities()

        // Then
        assert(result.size == 1)
        assert(result[0].id == 1L)
        assert(result[0].name == "Test Entity")
    }

    @Test
    fun `should delete entity successfully`() {
        // Given
        Mockito.doReturn(true).`when`(repository).existsById(1L)
        Mockito.doNothing().`when`(repository).deleteById(1L)

        // When & Then - Should not throw exception
        service.deleteEntity(1L)
    }
}
```

### 9.14 Service Layer Testing Checklist

Before completing any service test:
- [ ] Test follows Spring Boot 4 and Mockito 5 patterns
- [ ] Uses `@Mock` annotations and proper mock setup
- [ ] Uses `doReturn()` and `doThrow()` instead of `when()`
- [ ] Uses explicit type parameters for generic methods
- [ ] Uses try-catch for exception testing instead of assertThrows
- [ ] Includes all required fields in DTOs and entities
- [ ] Tests both success and failure scenarios
- [ ] Tests edge cases and boundary conditions
- [ ] Uses descriptive test names with backticks
- [ ] Follows Given-When-Then structure
- [ ] All tests pass with `./mvnw test -Dtest="*ServiceTest"`
