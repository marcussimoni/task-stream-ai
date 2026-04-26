---
version: 1.1.0
last_updated: 2026-04-26
author: AI Assistant
---

# Monthly Overview Feature - Product Requirement Document

## 1. Feature Name
**Monthly Overview** (renamed from Weekly Overview)

## 2. Overview / Objective
Transform the existing Monthly Overview feature to display tasks grouped by tag within a selected month. The feature allows users to navigate between months and view all tasks that overlap with the selected month, organized by tag in descending order of task count. Each tag section shows all associated tasks (completed and incomplete) for that month.

## 3. Target Users
- End users tracking daily routines
- Users wanting a broader view of their monthly task distribution
- Users who need to plan tasks across longer time periods

## 4. Functional Requirements

### Core Functionality
- **Month Navigation**: Previous/Next/Current month navigation controls
- **Default View**: Always loads the current month on initial access
- **Date Range Query**: Fetch tasks where `startDate ≤ monthEnd` AND `endDate ≥ monthStart`
- **Tag Grouping**: Tasks displayed in collapsible/expandable tag sections
- **Tag Sorting**: Tags sorted by descending task count (tags with most tasks first)
- **Task Sorting**: Tasks sorted alphabetically by name within each tag group
- **Stats Summary**: Display total task count, completed count, and pending count per tag

### Display Elements
- Month label showing selected month/year (e.g., "April 2026")
- Tag sections labeled by tag name with task count (e.g., "Coding (5 tasks)", "Learning (3 tasks)")
- Task items showing: name, description, status badge, tag, date range, progress bar
- Empty state when no tasks for the selected month
- Tags without tasks are not displayed

## 5. Non-Functional Requirements

- **Performance**: Load time under 1 second for typical monthly data
- **Responsiveness**: UI remains responsive during month navigation
- **Accessibility**: Maintain WCAG compliance with keyboard navigation
- **Maintainability**: Clean separation between calculation logic and presentation

## 6. Technical Requirements

### Frontend Changes
- Rename component: `WeeklyOverviewComponent` → `MonthlyOverviewComponent`
- Update route from `/weekly-overview` to `/monthly-overview`
- Update component selector from `app-weekly-overview` to `app-monthly-overview`
- Rename files: `weekly-overview.component.*` → `monthly-overview.component.*`
- Simplify frontend logic: consume pre-grouped data from backend
- Remove client-side grouping logic since backend handles it
- Update `TaskService` call: `getTasksForWeek()` → `getGroupedTasksByTags()`

### Backend/API (No DB changes required)
- **Use existing endpoint**: `GET /api/tasks/grouped-by-tags` accepting `month` parameter
- This endpoint returns pre-grouped tasks by tag with counts included

### Algorithm: Tag Grouping
```typescript
// Backend handles grouping - use GET /api/tasks/grouped-by-tags
// Frontend receives pre-grouped data by tag
// Sort tags by descending task count (already done by backend)
// Sort tasks alphabetically within each tag
// Filter out tags with no tasks (already done by backend)
```

## 7. UI / UX Specifications

### Layout
```
┌─────────────────────────────────────┐
│  Monthly Overview                   │
│  [← Previous]  [April 2026]  [Next →] │
│  [Current Month]                    │
├─────────────────────────────────────┤
│  Sort by: [Name] [Tag]              │
├─────────────────────────────────────┤
│  Coding (5 tasks) ▼                 │
│  ┌─────────────────────────────┐     │
│  │ Task A    [In Progress]    │     │
│  │ Task B    [Completed]      │     │
│  │ Task E    [Not Started]      │     │
│  └─────────────────────────────┘     │
│  Learning (3 tasks) ▲               │
│  ┌─────────────────────────────┐     │
│  │ Task C    [Not Started]      │     │
│  │ Task D    [Completed]      │     │
│  └─────────────────────────────┘     │
├─────────────────────────────────────┤
│  8 tasks | 3 completed | 5 pending  │
└─────────────────────────────────────┘
```

### Visual Requirements
- Tag sections should be visually distinct (card-style or bordered)
- Tag labels show tag name and task count with expand/collapse indicators
- Tasks maintain existing styling: progress bar, status badge, tag color
- Tags ordered by descending task count (most tasks first)
- Collapsible sections to show/hide tasks within each tag

## 8. Success Metrics
- User can navigate to any month and see tasks within 1 second
- Tasks are correctly grouped by tag within the selected month
- Tags are ordered by descending task count
- Component renders without console errors
- All existing task display features preserved

## 9. Edge Cases / Constraints

### Edge Cases
- **Empty month**: Display friendly empty state with hint text
- **Tag with no tasks**: Tags without tasks are not displayed
- **Task spanning multiple months**: Task appears in each month it overlaps with
- **Single task in tag**: Display tag section with single task
- **No tags assigned**: Handle tasks without tags appropriately

### Constraints
- **No backend/database changes** - use existing data structure
- Keep backward compatibility if other components use shared services
- Maintain existing sort controls (Name, Tag)

## 10. Implementation Notes

### Files to Modify
| File | Action |
|------|--------|
| `frontend/src/app/features/weekly-overview/weekly-overview.component.ts` | Rename to `monthly-overview.component.ts`, update logic |
| `frontend/src/app/features/weekly-overview/weekly-overview.component.html` | Rename to `monthly-overview.component.html`, update template |
| `frontend/src/app/features/weekly-overview/weekly-overview.component.css` | Rename to `monthly-overview.component.css` |
| `frontend/src/app/app.routes.ts` | Update route path from `weekly-overview` to `monthly-overview` |
| `frontend/src/app/core/services/task.service.ts` | May need new method `getTasksForMonth()` |

### Component Interface Changes
```typescript
// Before
export class WeeklyOverviewComponent {
  currentWeekStart: Date;
  currentWeekEnd: Date;
  weekLabel: string;
  loadTasksForWeek(): void;
}

// After
export class MonthlyOverviewComponent {
  currentMonth: Date;
  monthLabel: string;
  tagsInMonth: TagGroup[];
  loadTasksForMonth(): void;
}

interface TagGroup {
  tagName: string;
  taskCount: number;
  completedCount: number;
  pendingCount: number;
  tasks: Task[];
  isExpanded: boolean;
}
```

### API Endpoint
```
GET /api/tasks/grouped-by-tags?month={month}
Response: List<TasksGroupedDTO>
```
