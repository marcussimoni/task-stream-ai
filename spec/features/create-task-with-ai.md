---
version: 1.0.0
last_updated: 2026-04-21
author: AI / Developer
---

# PRD: Create Task with AI

## 1. Feature Name

**Create Task with AI** - "Create with AI" button and modal for AI-assisted task creation

---

## 2. Overview / Objective

### Problem Statement
Users currently need to manually fill out all task details (name, description, dates, priority, tags) when creating a new task. This process can be time-consuming and repetitive, especially for users who create many tasks regularly.

### Solution
Add a "Create with AI" button that opens a modal where users can describe what they want in natural language. The AI will interpret the request and return a preview of tasks to be created. Users can review and edit the tasks before confirming the creation. The actual task creation is a background fire-and-forget operation.

### Goals
- Reduce task creation time by allowing natural language input
- Provide transparency by showing AI-generated tasks before creation
- Allow users to edit AI-generated tasks before final confirmation
- Leverage existing AI infrastructure (`AiAssistantController`)
- Maintain consistency with current UI patterns
- Provide clear feedback on success or failure

---

## 3. Target Users

- **Primary**: All users who create tasks regularly
- **Secondary**: Users who prefer natural language over form-based input
- **Use Cases**:
  - Quick task creation with minimal typing
  - Complex task descriptions that are easier to express in natural language
  - Batch task creation requests

---

## 4. Functional Requirements

### 4.1 Frontend - Tasks Component

| ID | Requirement | Priority |
|----|-------------|----------|
| F1 | Add "Create with AI" button alongside existing "+ Create New Task" button | Must |
| F2 | Button must be visually distinct (different style/icon) to indicate AI functionality | Should |
| F3 | Clicking button opens a dedicated AI modal | Must |
| F4 | Modal contains a textarea for user prompt input | Must |
| F5 | Modal contains a "Generate Tasks" button to submit the prompt | Must |
| F6 | Modal contains a "Cancel" button to close without action | Must |
| F7 | Show loading state while waiting for backend response | Must |
| F8 | Display task preview list below textarea after AI response | Must |
| F9 | Each preview shows: name, tag, priority, start date, end date | Must |
| F10 | Allow inline editing of task preview fields | Should |
| F11 | Modal contains "Confirm Creation" button to finalize tasks | Must |
| F12 | Display success toast when tasks are submitted for creation | Must |
| F13 | Display error toast when backend returns error response | Must |
| F14 | Close modal only after tasks are submitted for creation or user cancels | Must |

### 4.2 Backend Integration

| ID | Requirement | Priority |
|----|-------------|----------|
| B1 | Step 1: Frontend calls `POST /ai-assistant/plan-automated-creation` endpoint | Must |
| B2 | Request body follows `AutomatedTaskDTO` structure: `{ "input": "user prompt" }` | Must |
| B3 | Backend returns list of `TaskRequestDTO` objects for preview | Must |
| B4 | Step 2: Frontend calls `POST /tasks/create-all` with edited task list | Must |
| B5 | Step 2 is fire-and-forget - frontend doesn't wait for completion | Must |
| B6 | No additional backend changes required - uses existing endpoints | Must |

### 4.3 Error Handling

| ID | Requirement | Priority |
|----|-------------|----------|
| E1 | Handle network errors with appropriate toast message | Must |
| E2 | Handle 4xx/5xx errors with appropriate toast message | Must |
| E3 | Keep modal open on error so user can retry | Should |
| E4 | Use existing `extractErrorMessage` pattern from `tasks.component.ts` | Must |

---

## 5. Non-Functional Requirements

| ID | Requirement | Priority |
|----|-------------|----------|
| NF1 | Follow existing Angular 21 patterns (signals, standalone components) | Must |
| NF2 | Reuse existing modal styling from `tasks.component.html` | Must |
| NF3 | Maintain responsive design (mobile/tablet/desktop) | Must |
| NF4 | Ensure accessibility (WCAG compliance) | Should |
| NF5 | No performance degradation on existing page load | Must |

---

## 6. Technical Requirements

### 6.1 Frontend Tech Stack
- **Framework**: Angular 21
- **Language**: TypeScript
- **State Management**: Angular Signals
- **HTTP Client**: Angular HttpClient with RxJS
- **Styling**: SCSS (reuse existing styles)

### 6.2 Backend Tech Stack
- **Framework**: Spring Boot 4
- **Language**: Kotlin
- **Endpoint**: `AiAssistantController.kt` - `POST /ai-assistant/plan-automated-creation`

### 6.3 API Contract

**Step 1 Endpoint**: `POST /ai-assistant/plan-automated-creation`

**Request Body**:
```json
{
  "input": "Create a high priority task to review the quarterly report by next Friday"
}
```

**Response**:
```json
[
  {
    "name": "Review Quarterly Report",
    "description": "Comprehensive review of Q3 financial report",
    "currentValue": 0,
    "startDate": "2026-04-28",
    "endDateInterval": 7,
    "endDate": "2026-05-05",
    "completed": false,
    "tagId": 1,
    "customEndDateSelected": true,
    "priority": "HIGH",
    "link": null
  }
]
```
- `200 OK` - Returns list of `TaskRequestDTO` objects for preview
- `400 Bad Request` - Invalid input
- `500 Internal Server Error` - Server error

**Step 2 Endpoint**: `POST /tasks/create-all`

**Request Body**: Array of edited `TaskRequestDTO` objects
```json
[
  {
    "name": "Review Quarterly Report",
    "description": "Comprehensive review of Q3 financial report",
    "currentValue": 0,
    "startDate": "2026-04-28",
    "endDateInterval": 7,
    "endDate": "2026-05-05",
    "completed": false,
    "tagId": 1,
    "customEndDateSelected": true,
    "priority": "HIGH",
    "link": null
  }
]
```

**Response**:
- `200 OK` - Tasks submitted for background processing (fire-and-forget)
- `400 Bad Request` - Invalid input
- `500 Internal Server Error` - Server error

---

## 7. UI / UX

### 7.1 Button Placement

```
[Create New Task] [Create with AI]
```

- Location: In the `create-button-section` div, alongside existing buttons
- Position: To the right of "+ Create New Task" button
- Style: Primary button style with AI indicator (magic wand or sparkle icon)

### 7.2 Modal Layout - Step 1 (Prompt Input)

```
┌─────────────────────────────────────┐
│  Create Task with AI           [×] │
├─────────────────────────────────────┤
│                                     │
│  Describe the task you want to     │
│  create:                            │
│                                     │
│  ┌─────────────────────────────┐   │
│  │                             │   │
│  │ [Textarea]                  │   │
│  │                             │   │
│  │                             │   │
│  └─────────────────────────────┘   │
│                                     │
│  [Cancel]        [Generate Tasks]  │
└─────────────────────────────────────┘
```

### 7.3 Modal Layout - Step 2 (Task Preview & Edit)

```
┌─────────────────────────────────────┐
│  Create Task with AI           [×] │
├─────────────────────────────────────┤
│                                     │
│  Describe the task you want to     │
│  create:                            │
│  ┌─────────────────────────────┐   │
│  │ "Create tasks for Q3 review │   │
│  │ and planning..."             │   │
│  └─────────────────────────────┘   │
│                                     │
│  ── Generated Tasks (3) ──────────── │
│                                     │
│  ┌─────────────────────────────┐   │
│  │ Task 1: [Editable Name]     │   │
│  │ Tag: [Dropdown] Priority:   │   │
│  │ [Dropdown]                  │   │
│  │ Start: [Date] End: [Date]   │   │
│  └─────────────────────────────┘   │
│                                     │
│  ┌─────────────────────────────┐   │
│  │ Task 2: [Editable Name]     │   │
│  │ Tag: [Dropdown] Priority:   │   │
│  │ [Dropdown]                  │   │
│  │ Start: [Date] End: [Date]   │   │
│  └─────────────────────────────┘   │
│                                     │
│  [Back]    [Confirm Creation]      │
└─────────────────────────────────────┘
```

### 7.4 Textarea Specifications

- **Rows**: 5-7 rows for comfortable multi-line input
- **Placeholder**: "Describe your task in natural language, e.g., 'Create a high priority task to review the quarterly report by next Friday'"
- **Character Limit**: None for initial release
- **Auto-focus**: Yes, on modal open
- **Read-only after generation**: Textarea becomes read-only after tasks are generated

### 7.5 Task Preview Specifications

- **Fields displayed**: Name (editable), Tag (dropdown), Priority (dropdown), Start Date (date picker), End Date (date picker)
- **Layout**: Each task in a separate card/section
- **Inline editing**: All fields are editable inline
- **Validation**: Maintain field validation rules (dates, required fields)
- **Responsive**: Stack vertically on mobile, side-by-side on desktop

### 7.6 Loading States

- **Step 1 Loading**: Disable "Generate Tasks" button, show spinner during AI processing
- **Step 2 Loading**: Disable "Confirm Creation" button, show spinner during task submission
- **Prevent modal close**: During both loading states
- **Overlay loading**: Optional overlay for better UX during longer operations

### 7.7 Toast Notifications

- **Step 1 Success**: "Tasks generated successfully! Review and edit before confirming."
- **Step 1 Error**: Display actual error message from backend AI endpoint
- **Step 2 Success**: "Tasks submitted for creation successfully!"
- **Step 2 Error**: Display actual error message from task creation endpoint

---

## 8. Success Metrics

| Metric | Target |
|--------|--------|
| Feature usage rate | 30% of task creations within 3 months |
| User satisfaction | Positive feedback on reduced creation time |
| Error rate | <5% of AI task creation attempts |
| Average time to create task | Reduced compared to manual form completion |

---

## 9. Edge Cases / Constraints

| Case | Handling |
|------|----------|
| Empty prompt submission | Disable "Generate Tasks" button until text entered |
| Backend AI timeout | Show error toast after configured timeout, allow retry |
| Network offline | Show error toast with network error message |
| Rapid button clicks | Debounce or disable button during both steps |
| Very long prompts | Accept all lengths, backend handles truncation if needed |
| AI returns no tasks | Show "No tasks could be generated" message, allow retry |
| AI returns invalid tasks | Show error toast, allow retry with different prompt |
| Task creation timeout | Fire-and-forget model, user notified on submission acceptance |
| User edits invalid data | Validate fields before submission, show inline errors |
| Date conflicts | Validate end date >= start date, show inline errors |

---

## 10. Implementation Notes

### 10.1 Files Affected

#### Frontend
1. **`frontend/src/app/features/tasks/tasks.component.html`**
   - Add "Create with AI" button in `create-button-section`
   - Add two-step AI modal template (prompt input + task preview)

2. **`frontend/src/app/features/tasks/tasks.component.ts`**
   - Add signals for AI modal state: `showAiModal`, `aiPrompt`, `generatedTasks`, `currentStep`
   - Add methods: `openAiModal()`, `closeAiModal()`, `generateTasks()`, `confirmTasks()`, `updateTask()`
   - Inject `AiAssistantService` and `TaskService`
   - Add error handling with toast for both steps

3. **`frontend/src/app/core/services/ai-assistant.service.ts`** (New File)
   - Create service to call `POST /ai-assistant/plan-automated-creation`
   - Method: `generateTasks(prompt: string): Observable<TaskRequestDTO[]>`

4. **`frontend/src/app/core/interfaces/task-request-dto.ts`** (New File)
   - Interface definition matching backend `TaskRequestDTO`

#### Backend
- No changes required - uses existing `AiAssistantController.kt` and `TaskController.kt`

### 10.2 Implementation Sequence

1. Create `TaskRequestDTO` interface in frontend core interfaces
2. Create `AiAssistantService` in frontend core services
3. Add step 1 functionality (prompt input + AI generation) to `TasksComponent`
4. Add step 2 functionality (task preview + editing + confirmation) to `TasksComponent`
5. Add "Create with AI" button to template
6. Add two-step AI modal template to `tasks.component.html`
7. Test both steps success and error flows
8. Verify task editing and validation work correctly
9. Verify toast notifications work correctly for both steps

### 10.3 Code Patterns to Follow

**Service Pattern** (from `@/frontend/src/app/core/services/task.service.ts`):
```typescript
@Injectable({ providedIn: 'root' })
export class AiAssistantService {
  private http = inject(HttpClient);
  private apiUrl = '/ai-assistant';
  
  generateTasks(prompt: string): Observable<TaskRequestDTO[]> {
    return this.http.post<TaskRequestDTO[]>(`${this.apiUrl}/plan-automated-creation`, { input: prompt });
  }
}
```

**Interface Pattern** (new file `task-request-dto.ts`):
```typescript
export interface TaskRequestDTO {
  name: string;
  description: string;
  currentValue: number;
  startDate: string;
  endDateInterval: number;
  endDate: string | null;
  completed: boolean;
  tagId: number;
  customEndDateSelected: boolean;
  priority: 'LOW' | 'MEDIUM' | 'HIGH';
  link: string | null;
}
```

**Component Pattern** (signals and modal handling from existing `tasks.component.ts`):
```typescript
// Signals
private showAiModalState = signal<boolean>(false);
readonly showAiModal = computed(() => this.showAiModalState());

private aiModalStepState = signal<1 | 2>(1);
readonly aiModalStep = computed(() => this.aiModalStepState());

private aiPromptState = signal<string>('');
readonly aiPrompt = computed(() => this.aiPromptState());

private generatedTasksState = signal<TaskRequestDTO[]>([]);
readonly generatedTasks = computed(() => this.generatedTasksState());

private isGeneratingTasksState = signal<boolean>(false);
readonly isGeneratingTasks = computed(() => this.isGeneratingTasksState());

private isCreatingTasksState = signal<boolean>(false);
readonly isCreatingTasks = computed(() => this.isCreatingTasksState());

// Methods
openAiModal(): void {
  this.showAiModalState.set(true);
  this.aiModalStepState.set(1);
  this.aiPromptState.set('');
  this.generatedTasksState.set([]);
}

closeAiModal(): void {
  if (!this.isGeneratingTasksState() && !this.isCreatingTasksState()) {
    this.showAiModalState.set(false);
  }
}

generateTasks(): void {
  const prompt = this.aiPromptState();
  if (!prompt?.trim()) return;
  
  this.isGeneratingTasksState.set(true);
  
  this.aiAssistantService.generateTasks(prompt).subscribe({
    next: (tasks) => {
      this.generatedTasksState.set(tasks);
      this.aiModalStepState.set(2);
      this.toastService.success('Tasks generated successfully! Review and edit before confirming.');
      this.isGeneratingTasksState.set(false);
    },
    error: (error) => {
      const message = this.extractErrorMessage(error);
      this.toastService.error(message, 'Error');
      this.isGeneratingTasksState.set(false);
    }
  });
}

confirmTasks(): void {
  const tasks = this.generatedTasksState();
  if (!tasks || tasks.length === 0) return;
  
  this.isCreatingTasksState.set(true);
  
  this.taskService.createTasks(tasks).subscribe({
    next: () => {
      this.toastService.success('Tasks submitted for creation successfully!');
      this.closeAiModal();
      this.isCreatingTasksState.set(false);
    },
    error: (error) => {
      const message = this.extractErrorMessage(error);
      this.toastService.error(message, 'Error');
      this.isCreatingTasksState.set(false);
    }
  });
}

updateTask(index: number, field: keyof TaskRequestDTO, value: any): void {
  const tasks = [...this.generatedTasksState()];
  tasks[index] = { ...tasks[index], [field]: value };
  this.generatedTasksState.set(tasks);
}

goBackToStep1(): void {
  this.aiModalStepState.set(1);
}
```

### 10.4 Testing Checklist

#### Step 1 Testing (Prompt Input & AI Generation)
- [ ] "Create with AI" button appears alongside "Create New Task"
- [ ] Clicking button opens modal with focused textarea (Step 1)
- [ ] Empty prompt disables "Generate Tasks" button
- [ ] Successful AI generation shows task preview list and moves to Step 2
- [ ] Failed AI generation shows error toast and stays in Step 1
- [ ] Cancel button closes modal without API call
- [ ] Modal overlay click closes modal (when not generating)
- [ ] "Generate Tasks" button is disabled and shows spinner during AI processing
- [ ] Textarea becomes read-only after tasks are generated

#### Step 2 Testing (Task Preview & Editing)
- [ ] Task preview list shows all generated tasks
- [ ] Each task displays: name, tag, priority, start date, end date
- [ ] All fields are editable inline
- [ ] Date validation prevents end date before start date
- [ ] "Back" button returns to Step 1, preserving original prompt
- [ ] "Confirm Creation" button submits edited tasks
- [ ] Successful task submission shows success toast and closes modal
- [ ] Failed task submission shows error toast and stays in Step 2
- [ ] "Confirm Creation" button is disabled and shows spinner during submission

#### General Testing
- [ ] Works on mobile, tablet, and desktop viewports
- [ ] Responsive layout adapts correctly for different screen sizes
- [ ] Modal can be closed with ESC key (when not processing)
- [ ] Rapid button clicks are properly debounced
- [ ] Network errors show appropriate toast messages
- [ ] Long prompts are handled correctly
- [ ] Empty task list from AI shows appropriate message
- [ ] Fire-and-forget behavior works for task creation (no waiting for completion)

---

## 11. Future Enhancements

| Enhancement | Description |
|-------------|-------------|
| SSE Notifications | Implement Server-Sent Events to notify user when background processing completes |
| Prompt History | Show previous prompts for quick re-use |
| Template Prompts | Provide common prompt templates (Meeting, Deadline, Project) |
| Advanced Task Editing | Add more fields to preview editing (description, links, etc.) |
| Task Grouping | Allow users to group related tasks during preview |
| Voice Input | Allow voice-to-text for prompt input |
| Batch Operations | Select/deselect multiple tasks for confirmation |
| AI Refinement | Allow users to ask AI to refine specific tasks |
| Task Dependencies | Add dependency relationships between tasks in preview |
| Real-time Collaboration | Share AI-generated task lists with team members |

---

## 12. References

- **Architecture Guide**: `@/spec/globals/architecture-guide.md`
- **Documentation Guidelines**: `@/spec/globals/documentation-guidelines.md`
- **Engineering Persona**: `@/spec/globals/engineering-persona.md`
- **Existing Tasks Component**: `@/frontend/src/app/features/tasks/tasks.component.html`
- **AI Assistant Controller**: `@/backend/src/main/kotlin/br/com/taskstreamai/controller/AiAssistantController.kt`
- **Task Controller**: `@/backend/src/main/kotlin/br/com/taskstreamai/controller/TaskController.kt`
- **Task DTO**: `@/backend/src/main/kotlin/br/com/taskstreamai/dto/TaskDTO.kt` (TaskRequestDTO)
