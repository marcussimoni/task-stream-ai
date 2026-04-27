# AI Workflow: Pre-Implementation Protocol

## Overview
This document defines the mandatory steps that must be executed **before starting any implementation or refactoring task** during the AI-assisted software development process.

The goal is to ensure clarity, alignment, and correctness before writing or modifying code.

---

## Execution Rules

The AI MUST follow this sequence strictly before implementing any change.

---

## Step 1: Clarification Phase

Before starting, the AI must analyze the request and ask clarifying questions when necessary.

### Guidelines
- Ask questions if:
  - Requirements are ambiguous
  - Business rules are unclear
  - Technical constraints are missing
  - There are multiple possible approaches

- Avoid unnecessary questions if the request is already clear.

### Examples
- "Should this feature be accessible only to authenticated users?"
- "Do you want this implemented using REST or GraphQL?"
- "Should this follow the existing project architecture?"

---

## Step 2: Implementation Plan

After clarification (or if no clarification is needed), the AI must present a structured plan.

### The plan must include:

#### 1. Scope
- What will be implemented or changed
- Affected layers (frontend, backend, or both)

#### 2. Technical Approach
- Key design decisions
- Architecture patterns (if applicable)
- Data flow overview

#### 3. Components / Modules
- Frontend:
  - Components
  - Services
  - State management (if needed)

- Backend:
  - Controllers
  - Services
  - Repositories
  - DTOs / Models

#### 4. API Design (if applicable)
- Endpoints
- Request/response structure

#### 5. Risks / Considerations
- Edge cases
- Performance concerns
- Security implications

---

## Step 3: User Confirmation (must have)

- If the change is significant, the AI should ask for confirmation before proceeding.
- Example:
  - "Do you want me to proceed with this plan?"

---

## Step 4: Implementation

- Only start coding AFTER completing Steps 1–3.
- Follow project standards and defined persona guidelines.

---

## Step 5: Build & Validation (must have)

After implementation, the AI MUST ensure the project builds successfully.

### Requirements

#### Frontend (Angular 21)
- Run build process:
  ```bash
  npm run build

  # AI Workflow: Implementation Protocol

## Rule 0 (CRITICAL)
- DO NOT start coding immediately
- ALWAYS follow the steps below

---

## Step 1: Understand the Request
- Summarize the feature in 1–2 sentences
- Identify if anything is unclear

---

## Step 2: Clarification (must have)
Ask questions ONLY if:
- Requirements are ambiguous
- Business rules are missing
- Multiple valid approaches exist

---

## Step 3: Implementation Plan

### 1. Scope
- What will be built or changed

### 2. Technical Approach
- Key decisions
- Data flow

### 3. Components / Modules
- Frontend
- Backend

### 4. API Design (if applicable)

### 5. Risks / Edge Cases

---

## Step 4: Task Breakdown (MANDATORY)
Break the work into SMALL executable steps.

Rules:
- Each task must be independently implementable
- Avoid large or vague tasks

---

## Step 5: Self-Review
Validate:
- Does the plan cover all PRD requirements?
- Is it overengineered?
- Is it consistent with the project?

---

## Step 6: Confirmation (for complex tasks)
Ask:
"Do you want me to proceed with this plan?"

---

## Step 7: Implementation
- Follow the plan and tasks strictly
- Do not deviate without justification

---

## Step 8: Validation

Ensure:
- Build passes
- No type errors
- Code is consistent with standards

---

## Controller Test Specification

### Overview
All controller tests must follow Spring Boot 4 and Mockito 5 patterns. This specification ensures consistency and maintainability across all controller tests.

### Required Test Dependencies
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

### Test Class Structure
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

### Test Patterns

#### 1. Happy Path Tests
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

#### 2. Error Handling Tests
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

#### 3. Validation Tests
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

### Mockito 5 Usage Guidelines

#### 1. Use `doReturn()` and `doThrow()` instead of `when()`
```kotlin
// ✅ CORRECT
Mockito.doReturn(expectedDTO).`when`(serviceService).createEntity(requestDTO)
Mockito.doThrow(RuntimeException("Error")).`when`(serviceService).createEntity(requestDTO)

// ❌ AVOID (causes issues with any() matchers)
Mockito.`when`(serviceService.createEntity(any())).thenReturn(expectedDTO)
```

#### 2. Use Direct Mock Objects Instead of `any()`
```kotlin
// ✅ CORRECT
Mockito.doReturn(expectedDTO).`when`(serviceService).createEntity(requestDTO)

// ❌ AVOID (causes NullPointer issues)
Mockito.doReturn(expectedDTO).`when`(serviceService).createEntity(any(CreateEntityRequestDTO::class.java))
```

#### 3. Void Method Stubbing
```kotlin
// ✅ CORRECT
Mockito.doNothing().`when`(serviceService).deleteEntity(1L)

// ❌ AVOID
Mockito.`when`(serviceService.deleteEntity(1L)).thenAnswer(Unit)
```

### MockMvc Assertion Patterns

#### 1. Status Assertions
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

#### 2. Content Assertions
```kotlin
mockMvc.get("/api/entities").andExpect {
    content().contentType(MediaType.APPLICATION_JSON)
    jsonPath("$.length()").value(2)
    jsonPath("$[0].name").value("Test Entity")
    jsonPath("$[0].id").value(1)
    content().string("")  // For empty responses
}
```

### DTO Construction Guidelines

#### 1. Include All Required Fields
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

#### 2. Use Proper Date/Time Types
```kotlin
// ✅ CORRECT
createdAt = LocalDateTime.now()
updatedAt = LocalDateTime.now()
startDate = LocalDate.now()

// ❌ AVOID (unless specifically required)
createdAt = "2024-01-01T00:00:00"
```

### Test Naming Convention

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

### Common Test Scenarios to Cover

1. **CRUD Operations**: Create, Read, Update, Delete
2. **Validation**: Empty input, invalid data
3. **Error Handling**: Service exceptions, database errors
4. **Content Type Verification**: Ensure JSON responses
5. **Status Code Verification**: Correct HTTP status codes

### File Structure

```
src/test/kotlin/br/com/taskstreamai/controller/
├── ControllerClassTest.kt
└── ...
```

### Required Test Execution

Before completing any controller implementation or refactoring:
1. Run `./mvnw test` to ensure all tests pass
2. Verify test coverage for new endpoints
3. Ensure no compilation errors related to Mockito usage
4. Confirm all tests follow the patterns above