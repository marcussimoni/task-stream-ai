# Task Priority Field - Product Requirements Document

## Overview

Add a required priority field to tasks with support for four levels (Low, Medium, High, Critical). This feature enables users to prioritize their tasks visually and organize their workflow by importance.

---

## Goals

1. Allow users to assign priority levels to tasks during creation and editing
2. Display priority visually in the task list with color-coded badges
3. Support full-stack implementation from database to frontend
4. Ensure backward compatibility with existing tasks (default to Low)
5. Follow clean code and modern Angular/Spring Boot practices

---

## Functional Requirements

### 1. Priority Levels

| Level | Color | Badge Background | Use Case |
|-------|-------|------------------|----------|
| Low | Green (#28a745) | #d4edda (light green) | Routine, non-urgent tasks |
| Medium | Blue (#007bff) | #cce5ff (light blue) | Standard priority tasks |
| High | Orange (#fd7e14) | #ffe5cc (light orange) | Important tasks |
| Critical | Red (#dc3545) | #f8d7da (light red) | Urgent, must-do tasks |

### 2. Display Behavior (Task List)
- **Position**: Priority badge displayed in task details section, next to tag
- **Visual style**: Colored badge with priority text (e.g., "HIGH")
- **Conditional display**: Always visible for all tasks

### 3. Display Behavior (Modal Form)
- **New section**: "Task Priority" section between "Tag and progress tracking" and modal footer
- **Input type**: Dropdown select with all four priority options
- **Required validation**: Field must be selected before submission
- **Default selection**: Medium (for new tasks)

### 4. Backend Compatibility
- **Default value**: Low for existing tasks during migration
- **Non-nullable**: Database column cannot be null

---

## Technical Requirements

### Database Changes

#### Migration
```sql
-- Add priority column to tasks table
ALTER TABLE tasks ADD COLUMN priority VARCHAR(10) NOT NULL DEFAULT 'LOW';

-- Create index for potential future filtering
CREATE INDEX idx_tasks_priority ON tasks(priority);
```

### Backend Changes

#### Task Entity Update
`src/main/kotlin/br/com/dailytrack/model/Task.kt`
```kotlin
@Entity
@Table(name = "tasks")
data class Task(
    // ... existing fields ...
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var priority: Priority = Priority.LOW
)

enum class Priority {
    LOW, MEDIUM, HIGH, CRITICAL
}
```

#### Task DTOs Update
`src/main/kotlin/br/com/dailytrack/dto/TaskDTO.kt`
```kotlin
data class TaskDTO(
    // ... existing fields ...
    val priority: Priority
)

data class TaskRequestDTO(
    // ... existing fields ...
    val priority: Priority = Priority.MEDIUM
)
```

#### Task Mapper Update
`src/main/kotlin/br/com/dailytrack/mapper/TaskMapper.kt`
- Add priority field mapping in `toDTO()` and `toEntity()` methods

### Frontend Changes

#### Task Model Update
`frontend/src/app/shared/models/task.model.ts`
```typescript
export interface Task {
  id?: number;
  name: string;
  description?: string;
  currentValue?: number;
  startDate?: string;
  endDateInterval?: number;
  endDate?: string;
  completed: boolean;
  tag: Tag;
  priority: Priority;
  customEndDateSelected: boolean;
}

export enum Priority {
  LOW = 'LOW',
  MEDIUM = 'MEDIUM',
  HIGH = 'HIGH',
  CRITICAL = 'CRITICAL'
}

export const PriorityConfig: Record<Priority, { label: string; color: string; bgColor: string }> = {
  [Priority.LOW]: { label: 'Low', color: '#28a745', bgColor: '#d4edda' },
  [Priority.MEDIUM]: { label: 'Medium', color: '#007bff', bgColor: '#cce5ff' },
  [Priority.HIGH]: { label: 'High', color: '#fd7e14', bgColor: '#ffe5cc' },
  [Priority.CRITICAL]: { label: 'Critical', color: '#dc3545', bgColor: '#f8d7da' }
};
```

#### Tasks Component Template Update
`frontend/src/app/features/tasks/tasks.component.html`

**Task List Display (lines 43-56):**
```html
<div class="task-meta">            
  <span *ngIf="task.tag" class="tag-badge" [style.background-color]="task.tag.color + '20'" [style.color]="task.tag.color">
    {{ task.tag.name }}
  </span>
  <span class="priority-badge" 
        [style.background-color]="getPriorityColor(task.priority) + '20'"
        [style.color]="getPriorityColor(task.priority)">
    {{ task.priority }}
  </span>
  <span *ngIf="task.startDate">Start Date: {{ task.startDate | date:'mediumDate' }}</span>
  <span>|</span>
  <span>End date: {{ task.endDate | date:'mediumDate' }}</span>
  <span>-</span>
  <span *ngIf="task.endDateInterval">{{task.endDateInterval}} Week</span>
  <span *ngIf="!task.endDateInterval">
    - Custom period
  </span>
</div>
```

**Modal Form New Section (after line 189, before modal-footer):**
```html
<div class="line"></div>

<h4>Task Priority</h4>

<div class="form-group">
  <label for="priority">Priority *</label>
  <select
    id="priority"
    class="form-control"
    formControlName="priority"
    [class.is-invalid]="taskForm.get('priority')?.invalid && taskForm.get('priority')?.touched">
    <option value="">Select priority</option>
    <option *ngFor="let priority of priorities" [value]="priority">
      {{ getPriorityLabel(priority) }}
    </option>
  </select>
  <div *ngIf="taskForm.get('priority')?.invalid && taskForm.get('priority')?.touched" class="invalid-feedback">
    Priority is required
  </div>
</div>
```

#### Tasks Component TypeScript Update
`frontend/src/app/features/tasks/tasks.component.ts`

**Add imports and properties:**
```typescript
import { Priority, PriorityConfig } from '../../shared/models/task.model';

export class TasksComponent {
  priorities = Object.values(Priority);
  
  // ... existing code ...
}
```

**Update taskForm initialization:**
```typescript
this.taskForm = this.fb.group({
  // ... existing fields ...
  priority: ['', Validators.required] // Default to empty for validation
});
```

**Add helper methods:**
```typescript
getPriorityColor(priority: Priority): string {
  return PriorityConfig[priority]?.color || '#6c757d';
}

getPriorityLabel(priority: Priority): string {
  return PriorityConfig[priority]?.label || priority;
}
```

**Update editTask method:**
```typescript
editTask(task: Task): void {
  this.isEditing = true;
  this.currentTaskId = task.id;
  this.taskForm.patchValue({
    // ... existing fields ...
    priority: task.priority || Priority.LOW
  });
  this.showModal = true;
}
```

#### Tasks Component Styles Update
`frontend/src/app/features/tasks/tasks.component.scss`

Add priority badge styling:
```scss
.priority-badge {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 0.75rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.task-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}
```

---

## Implementation Plan

### Phase 1: Backend Changes
1. Create Priority enum in model package
2. Add priority field to Task entity with @Enumerated(EnumType.STRING)
3. Update TaskDTO and TaskRequestDTO with priority field
4. Update TaskMapper to map priority field
5. Create database migration script

### Phase 2: Frontend Model Updates
1. Add Priority enum and PriorityConfig to task.model.ts
2. Update Task interface to include priority field

### Phase 3: Frontend Component Updates
1. Update TasksComponent taskForm to include priority validator
2. Add priority form section to modal template
3. Add priority badge display to task list template
4. Implement getPriorityColor() and getPriorityLabel() helper methods
5. Update editTask() to populate priority field
6. Add priority badge SCSS styling

### Phase 4: Testing & Validation
1. Run database migration
2. Verify existing tasks default to LOW priority
3. Test task creation with all priority levels
4. Test task editing with priority changes
5. Verify priority badges display correctly with proper colors

---

## Acceptance Criteria

- [ ] Database migration adds priority column with LOW as default
- [ ] All existing tasks have priority set to LOW after migration
- [ ] Priority field is required in task creation/edit modal
- [ ] Priority dropdown shows all four options (Low, Medium, High, Critical)
- [ ] New tasks default to Medium priority in the form
- [ ] Priority badge displays in task list with correct color coding
- [ ] Task creation works with all priority levels
- [ ] Task editing works with priority changes
- [ ] Backend returns priority in TaskDTO responses
- [ ] Backend accepts priority in TaskRequestDTO requests

