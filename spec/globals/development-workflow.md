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

---

## Service Layer Testing Workflow

### Overview
All service layer tests must follow Spring Boot 4 and Mockito 5 patterns. This workflow ensures consistency and maintainability across all service tests.

### Step 1: Test Setup
```kotlin
// Required dependencies
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertThrows
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

### Step 2: Test Class Structure
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
}
```

### Step 3: Test Data Setup
```kotlin
private lateinit var testTag: Tag
private lateinit var testEntity: Entity

@BeforeEach
fun setup() {
    // Mock setup
    // ...
    
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
```

### Step 4: Test Implementation Patterns

#### 4.1 Happy Path Tests
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
        tag = TagDTO(id = 1L, name = "Work", description = "Work tasks", color = "#FF0000", createdAt = LocalDateTime.now(), updatedAt = LocalDateTime.now()),
        createdAt = LocalDateTime.now(),
        updatedAt = LocalDateTime.now()
    )
    
    Mockito.doReturn(Optional.of(testTag)).`when`(repository).findById(1L)
    Mockito.doReturn(testEntity).`when`(repository).save(org.mockito.ArgumentMatchers.any(Entity::class.java))
    Mockito.doReturn(expectedDTO).`when`(mapper).toDTO(testEntity)

    // When
    val result = service.createEntity(requestDTO)

    // Then
    assert(result.id == 1L)
    assert(result.name == "Test Entity")
    assert(result.description == "Test Description")
}
```

#### 4.2 Exception Handling Tests
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

#### 4.3 Edge Case Tests
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
```

### Step 5: Mockito 5 Best Practices

#### 5.1 Use `doReturn()` and `doThrow()` instead of `when()`
```kotlin
// ✅ CORRECT
Mockito.doReturn(expectedDTO).`when`(repository).findById(1L)
Mockito.doThrow(RuntimeException("Database error")).`when`(repository).save(org.mockito.ArgumentMatchers.any())

// ❌ AVOID
Mockito.`when`(repository.findById(any())).thenReturn(Optional.of(entity))
```

#### 5.2 Use Explicit Type Parameters
```kotlin
// ✅ CORRECT
Mockito.doReturn(Optional.empty<Entity>()).`when`(repository).findById(999L)
Mockito.doReturn(Optional.empty<Tag>()).`when`(tagRepository).findById(999L)

// ❌ AVOID
Mockito.doReturn(Optional.empty()).`when`(repository).findById(999L)
```

#### 5.3 Use `ArgumentMatchers.any()` with Explicit Types
```kotlin
// ✅ CORRECT
Mockito.doReturn(entity).`when`(repository).save(org.mockito.ArgumentMatchers.any(Entity::class.java))

// ❌ AVOID
Mockito.doReturn(entity).`when`(repository).save(org.mockito.ArgumentMatchers.any())
```

### Step 6: Exception Testing Strategy

#### Use Try-Catch Instead of assertThrows
```kotlin
// ✅ CORRECT
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

// ❌ AVOID (causes type inference issues)
@Test
fun `should throw exception when entity not found`() {
    // Given
    Mockito.doReturn(Optional.empty<Entity>()).`when`(repository).findById(999L)

    // When & Then
    assertThrows<ResourceNotFoundException> {
        service.getEntityById(999L)
    }
}
```

### Step 7: Test Execution Workflow

#### 7.1 Before Running Tests
1. **Verify Mock Setup**: Ensure all mocks are properly configured
2. **Check Dependencies**: Confirm all required imports are present
3. **Validate Test Data**: Ensure test objects include all required fields

#### 7.2 Running Tests
```bash
# Run all service tests
./mvnw test -Dtest="*ServiceTest"

# Run specific service test
./mvnw test -Dtest="ServiceClassTest"

# Run tests with detailed output
./mvnw test -Dtest="*ServiceTest" -X
```

#### 7.3 After Running Tests
1. **Check Compilation**: Ensure no compilation errors
2. **Verify Test Results**: All tests should pass
3. **Review Coverage**: Ensure adequate test coverage
4. **Fix Issues**: Address any failing tests immediately

### Step 8: Common Issues and Solutions

#### 8.1 Type Inference Issues
**Problem**: `Cannot infer type for type parameter 'T'`
**Solution**: Use explicit type parameters for generic methods
```kotlin
Mockito.doReturn(Optional.empty<Entity>()).`when`(repository).findById(999L)
```

#### 8.2 NullPointerException in Mocks
**Problem**: Mock returns null unexpectedly
**Solution**: Ensure proper mock configuration with `doReturn()`
```kotlin
Mockito.doReturn(entity).`when`(repository).save(org.mockito.ArgumentMatchers.any(Entity::class.java))
```

#### 8.3 assertThrows Type Issues
**Problem**: Type inference issues with assertThrows
**Solution**: Use try-catch pattern instead
```kotlin
try {
    service.getEntityById(999L)
    assert(false) { "Should have thrown ResourceNotFoundException" }
} catch (e: ResourceNotFoundException) {
    // Expected exception
}
```

### Step 9: Testing Checklist

Before completing any service test implementation:

#### 9.1 Code Quality
- [ ] Test follows Spring Boot 4 and Mockito 5 patterns
- [ ] Uses `@Mock` annotations and proper mock setup
- [ ] Uses `doReturn()` and `doThrow()` instead of `when()`
- [ ] Uses explicit type parameters for generic methods
- [ ] Uses try-catch for exception testing instead of assertThrows

#### 9.2 Test Coverage
- [ ] Tests both success and failure scenarios
- [ ] Tests edge cases and boundary conditions
- [ ] Tests CRUD operations (Create, Read, Update, Delete)
- [ ] Tests business logic and validation
- [ ] Tests service-to-service integration

#### 9.3 Test Structure
- [ ] Uses descriptive test names with backticks
- [ ] Follows Given-When-Then structure
- [ ] Includes all required fields in DTOs and entities
- [ ] Uses proper date/time types (LocalDateTime, LocalDate)
- [ ] Groups related tests together

#### 9.4 Execution Validation
- [ ] All tests pass with `./mvnw test -Dtest="*ServiceTest"`
- [ ] No compilation errors related to Mockito usage
- [ ] No type inference issues
- [ ] Test execution is fast and focused

### Step 10: Complex Service Handling

For services with complex external dependencies (like AI services):

#### 10.1 Simplified Testing Strategy
- Focus on basic functionality testing
- Test error handling and edge cases
- Avoid complex mocking scenarios
- Consider integration tests for complex flows

#### 10.2 Example: AI Service Testing
```kotlin
@Test
fun `should initialize service correctly`() {
    // Given - Service is initialized in setup
    
    // When
    val service = aiAssistantService

    // Then - Service should be properly initialized
    assert(service != null)
}

@Test
fun `should handle AI service exceptions gracefully`() {
    // Given
    Mockito.doThrow(RuntimeException("AI service unavailable")).`when`(createTaskChatClient).prompt(org.mockito.ArgumentMatchers.any<String>())
    
    // When
    val result = aiAssistantService.planAutomatedTaskCreation(AutomatedTaskDTO("test input"))

    // Then - Should handle exceptions gracefully
    assert(result == null)
}
```

### Step 11: Continuous Integration

#### 11.1 Pre-commit Checks
1. Run service tests: `./mvnw test -Dtest="*ServiceTest"`
2. Verify no compilation errors
3. Check test coverage metrics
4. Ensure all tests follow established patterns

#### 11.2 Code Review Guidelines
- Review test coverage for new service methods
- Verify Mockito 5 compliance
- Check for proper exception handling
- Ensure test naming conventions are followed

### Step 12: Documentation Updates

When adding new service tests:

1. **Update Test Documentation**: Add new test patterns to this document
2. **Update Architecture Guidelines**: Reference new testing strategies
3. **Update Development Workflow**: Include any new testing workflows
4. **Create Examples**: Provide example tests for complex scenarios

### Required Test Execution

Before completing any service implementation or refactoring:
1. Run `./mvnw test -Dtest="*ServiceTest"` to ensure all service tests pass
2. Verify test coverage for new service methods
3. Ensure no compilation errors related to Mockito usage
4. Confirm all tests follow the patterns above
5. Check all items in the testing checklist (Step 9)