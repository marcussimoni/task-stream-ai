---
version: 1.0.0
last_updated: 2026-05-01
author: AI / Developer
---

# PRD: Estimated Read Time for Task Content

## 1. Feature Name

**Estimated Read Time for Task Content** - Backend data model enhancement to support content analysis and reading time estimation

---

## 2. Overview / Objective

### Problem Statement
The current Task entity lacks fields to store content analysis data such as word count, technical depth, and estimated reading time. This information is valuable for users to understand the complexity and time investment required for task-related content (summaries, linked articles, documentation).

### Solution
Add estimated read time fields to the Task entity with proper database migration, ensuring production compatibility through nullable columns. The data population will be handled by the existing `AiAssistantService` which analyzes task content and provides reading time estimates.

### Goals
- Extend Task entity with content analysis fields
- Create production-safe database migration
- Maintain backward compatibility
- Support AI-powered content analysis
- Enable future frontend display features

---

## 3. Target Users

- **Primary**: Development team working on task management features
- **Secondary**: AI systems that analyze task content
- **Use Cases**:
  - Store reading time estimates for task summaries
  - Track technical complexity of linked content
  - Support content analysis workflows
  - Enable future user-facing time estimates

---

## 4. Functional Requirements

### 4.1 Data Model Enhancement

| ID | Requirement | Priority |
|----|-------------|----------|
| F1 | Add totalWordCount field to Task entity | Must |
| F2 | Add technicalDepth enum field to Task entity | Must |
| F3 | Add estimatedReadingTimeMinutes field to Task entity | Must |
| F4 | Add depthJustification field to Task entity | Should |
| F5 | Add recommendedPace field to Task entity | Should |
| F6 | Ensure all new fields are nullable for production compatibility | Must |

### 4.2 Database Migration

| ID | Requirement | Priority |
|----|-------------|----------|
| F7 | Create Flyway migration script for new columns | Must |
| F8 | Use proper column naming convention (snake_case) | Must |
| F9 | Set all columns as nullable to maintain compatibility | Must |
| F10 | Follow existing migration patterns and H2 compatibility | Must |

### 4.3 DTO and Mapper Updates

| ID | Requirement | Priority |
|----|-------------|----------|
| F11 | Update TaskDTO to include estimated time fields | Must |
| F12 | Update TaskRequestDTO to include estimated time fields | Must |
| F13 | Update TaskMapper bidirectional mapping | Must |
| F14 | Maintain TechnicalDepth enum consistency | Must |

---

## 5. Non-Functional Requirements

| ID | Requirement | Priority |
|----|-------------|----------|
| NF1 | Follow existing JPA entity patterns | Must |
| NF2 | Maintain production database compatibility | Must |
| NF3 | Use proper Kotlin null safety | Must |
| NF4 | Follow existing DTO naming conventions | Must |
| NF5 | Ensure proper enum serialization | Must |

---

## 6. Technical Requirements

### 6.1 Backend Tech Stack
- **Framework**: Spring Boot 4
- **Language**: Kotlin
- **Database**: H2 with Flyway migrations
- **ORM**: Spring Data JPA

### 6.2 Data Model Structure

#### New Task Entity Fields
```kotlin
// Estimated time fields for content analysis
@Column(name = "total_word_count")
var totalWordCount: Int? = null,

@Column(name = "technical_depth")
@Enumerated(EnumType.STRING)
var technicalDepth: TechnicalDepth? = null,

@Column(name = "estimated_reading_time_minutes")
var estimatedReadingTimeMinutes: Int? = null,

@Column(name = "depth_justification", length = 500)
var depthJustification: String? = null,

@Column(name = "recommended_pace", length = 200)
var recommendedPace: String? = null
```

#### TechnicalDepth Enum
```kotlin
enum class TechnicalDepth {
    LOW,    // Simple content, quick reading
    MEDIUM, // Moderate complexity, standard reading pace
    HIGH    // Complex technical content, requires careful reading
}
```

### 6.3 Database Schema

#### Migration Script Structure
```sql
-- Add estimated time fields to tasks table
-- All fields are nullable for production database compatibility

ALTER TABLE daily_track.tasks ADD COLUMN total_word_count INTEGER NULL;
ALTER TABLE daily_track.tasks ADD COLUMN technical_depth VARCHAR(10) NULL;
ALTER TABLE daily_track.tasks ADD COLUMN estimated_reading_time_minutes INTEGER NULL;
ALTER TABLE daily_track.tasks ADD COLUMN depth_justification VARCHAR(500) NULL;
ALTER TABLE daily_track.tasks ADD COLUMN recommended_pace VARCHAR(200) NULL;
```

---

## 7. Implementation Notes

### 7.1 Files Affected

#### Backend Files
1. **`backend/src/main/kotlin/br/com/taskstreamai/model/Task.kt`**
   - Add estimated time fields (all nullable)
   - Include proper JPA annotations
   - Follow existing entity patterns

2. **`backend/src/main/kotlin/br/com/taskstreamai/model/TechnicalDepth.kt`** (New File)
   - Create TechnicalDepth enum
   - Define LOW, MEDIUM, HIGH values

3. **`backend/src/main/resources/db/migration/V11__add_estimated_time_fields_to_tasks.sql`** (New File)
   - Add all estimated time columns
   - Ensure nullable constraints
   - Follow H2 compatibility patterns

4. **`backend/src/main/kotlin/br/com/taskstreamai/dto/TaskDTO.kt`**
   - Add estimated time fields to TaskDTO
   - Include proper JSON annotations
   - Maintain existing structure

5. **`backend/src/main/kotlin/br/com/taskstreamai/dto/TaskRequestDTO.kt`**
   - Add estimated time fields to TaskRequestDTO
   - Include property descriptions
   - Set nullable defaults

6. **`backend/src/main/kotlin/br/com/taskstreamai/mapper/TaskMapper.kt`**
   - Update toEntity() method to handle new fields
   - Update toDTO() method to handle new fields
   - Maintain bidirectional mapping

7. **`backend/src/main/kotlin/br/com/taskstreamai/dto/EstimatedTimeDTO.kt`**
   - Update to use shared TechnicalDepth enum
   - Remove duplicate enum definition

### 7.2 Implementation Sequence

#### Step 1: Model Layer
1. Create TechnicalDepth enum in model package
2. Add estimated time fields to Task entity
3. Ensure proper JPA annotations and nullability

#### Step 2: Database Migration
1. Create Flyway migration script V11
2. Add all columns as nullable
3. Test migration on development database

#### Step 3: DTO Layer
1. Update TaskDTO with new fields
2. Update TaskRequestDTO with new fields
3. Update EstimatedTimeDTO to use shared enum

#### Step 4: Mapper Layer
1. Update TaskMapper.toEntity() method
2. Update TaskMapper.toDTO() method
3. Test bidirectional mapping

### 7.3 Data Population

The estimated time fields will be populated by the existing `AiAssistantService` which:
- Analyzes task content and linked URLs
- Calculates word count and reading time estimates
- Determines technical depth based on content complexity
- Provides depth justification and reading pace recommendations

---

## 8. Success Metrics

| Metric | Target |
|--------|--------|
| Task entity enhancement | Successfully updated with all estimated time fields |
| Database migration | Successfully applied without production issues |
| Production compatibility | 100% backward compatibility maintained |
| DTO mapping | Proper bidirectional mapping for all new fields |
| Code quality | Follows existing patterns and conventions |

---

## 9. Edge Cases / Constraints

| Case | Handling |
|------|----------|
| Production deployment | All fields nullable to prevent deployment issues |
| Existing tasks | All new fields will be null for existing records |
| Data validation | No validation required as fields are optional |
| Enum serialization | Proper JSON serialization for TechnicalDepth enum |
| Database constraints | No foreign key constraints on new fields |

---

## 10. Dependencies

### 10.1 External Dependencies
- None - this is a self-contained data model enhancement

### 10.2 Internal Dependencies
- **AiAssistantService**: Responsible for populating estimated time data
- **Existing Task entity**: Base entity being enhanced
- **Flyway**: Database migration framework

---

## 11. Future Enhancements

| Enhancement | Description |
|-------------|-------------|
| Frontend Display | Show estimated reading time in task interface |
| Tasks List Enhancement | Display estimated time in minutes beside progress percentage (e.g., "progress: 0 / 100 (0%) Estimated read time: 10 min") with information icon for details |
| Advanced Analytics | Track reading time patterns across tasks |
| Content Analysis | Enhanced AI analysis for better estimates |
| User Preferences | Allow users to set reading pace preferences |
| Time Tracking | Track actual vs estimated reading times |

---

## 11. Frontend Implementation

### 11.1 Component Structure Updates

| File | Changes Made |
|------|-------------|
| **`frontend/src/app/core/models/task.model.ts`** | Added estimated time fields to Task interface: `totalWordCount`, `technicalDepth`, `estimatedReadingTimeMinutes`, `depthJustification`, `recommendedPace` |
| **`frontend/src/app/features/tasks/tasks.component.html`** | Enhanced task display with estimated time beside progress percentage and information icon for detailed modal |
| **`frontend/src/app/features/tasks/tasks.component.css`** | Added comprehensive styling for estimated time display components and modal |
| **`frontend/src/app/features/tasks/tasks.component.ts`** | Added modal state management and `showEstimatedTimeDetails()`, `closeEstimatedTimeModal()` methods |

### 11.2 Display Format Implementation

#### Task List Display
- **Format**: `"progress: 0 / 100 (0%) Estimated read time: 10 min"`
- **Conditional Display**: Only shows when `task.estimatedReadingTimeMinutes` exists
- **Information Icon**: Button with `fa-solid fa-info-circle` icon triggers detailed modal

#### Estimated Time Details Modal
- **Modal Header**: "Estimated Time Details" with close button
- **Content Display**:
  - Estimated Reading Time: X minutes
  - Total Word Count: X words (if available)
  - Technical Depth: LOW/MEDIUM/HIGH (if available)
  - Depth Justification: AI-provided explanation (if available)
  - Recommended Pace: Reading pace recommendation (if available)
- **Modal Footer**: Single "Close" button

### 11.3 CSS Styling Implementation

| Class | Purpose | Key Properties |
|--------|---------|----------------|
| `.estimated-time-container` | Main container for time display | `display: flex`, `align-items: center`, `gap: 0.5rem` |
| `.estimated-time-label` | Time text styling | `font-size: 0.875rem`, `color: #6c757d`, `font-weight: 500` |
| `.info-btn` | Information button | `background-color: transparent`, `border-color: #17a2b8`, `hover: #17a2b8 background` |
| `.time-details` | Modal content container | `display: flex`, `flex-direction: column`, `gap: 1rem` |
| `.detail-row` | Individual detail rows | `display: flex`, `justify-content: space-between`, `border-bottom: 1px solid #e9ecef` |
| `.detail-row strong` | Detail labels | `color: #495057`, `font-weight: 600`, `min-width: 150px` |
| `.detail-row span` | Detail values | `color: #6c757d`, `flex: 1` |

### 11.4 Component Logic Implementation

#### State Management
```typescript
// Estimated time details state
private selectedTaskForTimeDetailsState = signal<Task | null>(null);
readonly selectedTaskForTimeDetails = computed(() => this.selectedTaskForTimeDetailsState());

// Modal visibility state
private showEstimatedTimeModalState = signal<boolean>(false);
readonly showEstimatedTimeModal = computed(() => this.showEstimatedTimeModalState());
```

#### Key Methods
```typescript
showEstimatedTimeDetails(task: Task): void {
  this.selectedTaskForTimeDetailsState.set(task);
  this.showEstimatedTimeModalState.set(true);
}

closeEstimatedTimeModal(): void {
  this.showEstimatedTimeModalState.set(false);
  this.selectedTaskForTimeDetailsState.set(null);
}
```

### 11.5 Integration Points

- **Data Flow**: Backend `TaskDTO` → Frontend `Task` model → UI display
- **Conditional Rendering**: Uses `*ngIf="task.estimatedReadingTimeMinutes"` for performance
- **User Interaction**: Click handler on information icon opens modal with full analysis
- **Consistent Styling**: Follows existing modal patterns and design system
- **TypeScript Safety**: All new fields are optional (`?`) to handle missing data
- **Angular Signals**: Modern reactive state management for performance

---

## 12. References

- **Architecture Guide**: `@/spec/globals/architecture-guidelines.md`
- **Documentation Guidelines**: `@/spec/globals/documentation-guidelines.md`
- **Engineering Persona**: `@/spec/globals/engineering-persona.md`
- **Task Entity**: `@/backend/src/main/kotlin/br/com/taskstreamai/model/Task.kt`
- **AiAssistantService**: `@/backend/src/main/kotlin/br/com/taskstreamai/service/AiAssistantService.kt`
- **EstimatedTimeDTO**: `@/backend/src/main/kotlin/br/com/taskstreamai/dto/EstimatedTimeDTO.kt`
- **TaskMapper**: `@/backend/src/main/kotlin/br/com/taskstreamai/mapper/TaskMapper.kt`
- **Frontend Tasks Component**: `@/frontend/src/app/features/tasks/tasks.component.ts`
- **Frontend Tasks HTML**: `@/frontend/src/app/features/tasks/tasks.component.html`
- **Frontend Tasks CSS**: `@/frontend/src/app/features/tasks/tasks.component.css`
