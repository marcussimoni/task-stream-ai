---
version: 1.0.0
last_updated: 2026-04-17
author: AI / Developer
---

# PRD: Tasks Component Weekly Grouping Refactor

## 1. Feature Name

Tasks Component Weekly Grouping Layout Refactor

## 2. Overview / Objective

Refactor the existing `tasks.component` to adopt a weekly-grouped layout similar to the `monthly-overview.component`. Currently, tasks are displayed as a flat list. This refactor will organize tasks into week-based buckets (Monday-Sunday) within the current month view, improving visual organization and temporal context for users.

**Goals:**
- Group tasks by calendar week (Monday-Sunday)
- Display week ranges (e.g., "Jan 1 - Jan 7") as section headers
- Add month navigation (previous/next/current) to browse tasks across months
- Preserve all existing CRUD functionality (Create, Read, Update, Delete, Complete)
- Maintain existing filters (tag filter, completed tasks toggle)

## 3. Target Users

- **Primary:** End users managing tasks who need better temporal organization
- **Secondary:** Users viewing task distribution across weeks for planning purposes

## 4. Functional Requirements

### 4.1 Week Grouping
| ID | Requirement | Priority |
|---|---|---|
| FR-001 | Display tasks grouped by calendar week (Monday start) | Must |
| FR-002 | Show week label as date range (e.g., "Apr 14 - Apr 20") | Must |
| FR-003 | Show task count per week | Should |
| FR-004 | Filter out weeks with no tasks (configurable) | Could |

### 4.2 Month Navigation
| ID | Requirement | Priority |
|---|---|---|
| FR-005 | Display current month/year header | Must |
| FR-006 | Previous month button (left arrow) | Must |
| FR-007 | Next month button (right arrow) | Must |
| FR-008 | "Current Month" quick-return button | Must |

### 4.3 Data Loading
| ID | Requirement | Priority |
|---|---|---|
| FR-009 | Load tasks for the displayed month only | Must |
| FR-010 | Automatically refresh when month changes | Must |

### 4.4 Existing Feature Preservation
| ID | Requirement | Priority |
|---|---|---|
| FR-011 | Preserve Create New Task button and modal | Must |
| FR-012 | Preserve Edit task functionality | Must |
| FR-013 | Preserve Delete task functionality | Must |
| FR-014 | Preserve Mark Complete/Uncomplete functionality | Must |
| FR-015 | Preserve Tag filter dropdown | Must |
| FR-016 | Preserve "Show/Hide completed tasks" toggle | Must |
| FR-017 | Preserve Weekly Calendar link | Must |
| FR-018 | Preserve task card styling (progress bars, badges, metadata) | Must |

## 5. Non-Functional Requirements

| ID | Requirement | Priority |
|---|---|---|
| NFR-001 | Maintain existing page load performance (< 2s) | Must |
| NFR-002 | Responsive layout for mobile/tablet/desktop | Should |
| NFR-003 | No breaking changes to task data model | Must |
| NFR-004 | Maintain existing accessibility standards | Should |

## 6. Technical Requirements

### 6.1 Data Model
```typescript
// WeekGroup interface (reused from monthly-overview)
interface WeekGroup {
  weekStart: Date;
  weekEnd: Date;
  weekLabel: string;
  tasks: Task[];
}
```

### 6.2 State Management
```typescript
// New state properties to add
weeksInMonth: WeekGroup[] = [];
currentMonth: Date = new Date();
monthLabel: string = '';
```

### 6.3 Methods to Port/Adapt from MonthlyOverviewComponent
- `calculateWeeksInMonth()` - Generate week buckets
- `distributeTasksIntoWeeks()` - Assign tasks to weeks
- `getMonthStart()`, `getMonthEnd()` - Date boundary helpers
- `formatDateForApi()` - API date formatting
- `previousMonth()`, `nextMonth()`, `goToCurrentMonth()` - Navigation

### 6.4 API Endpoints
- **Existing:** `GET /api/tasks?tagId={id}` → Change to `GET /api/tasks/month?start={date}&end={date}&tagId={id}`
- Or filter client-side if `getAllTasks()` is retained

## 7. UI / UX

### 7.1 Layout Structure
```
┌─────────────────────────────────────────┐
│  Tasks                    [Create New]  │
│                                         │
│  [←]   April 2025   [→]  [Current]      │
│                                         │
│  [Tag ▼] [Show Completed] [Calendar]    │
│                                         │
│  ┌─────────────────────────────────────┐  │
│  │ Week: Apr 14 - Apr 20 (3 tasks)     │  │
│  ├─────────────────────────────────────┤  │
│  │ [Task Card 1]                       │  │
│  │ [Task Card 2]                       │  │
│  │ [Task Card 3]                       │  │
│  └─────────────────────────────────────┘  │
│                                         │
│  ┌─────────────────────────────────────┐  │
│  │ Week: Apr 21 - Apr 27 (1 task)      │  │
│  ├─────────────────────────────────────┤  │
│  │ [Task Card 4]                       │  │
│  └─────────────────────────────────────┘  │
└─────────────────────────────────────────┘
```

### 7.2 Week Header Styling
- Distinct background color (similar to monthly-overview)
- Week date range label (left-aligned)
- Task count badge (right-aligned)

### 7.3 Task Cards
- **No visual changes** - Preserve existing task-item styling
- Maintain: title, description, tag badge, priority badge, dates, progress bar, action buttons

### 7.4 Empty States
- If no tasks in month: "No tasks planned for this month"
- If filtered results empty: "No tasks match the current filters"

## 8. Success Metrics

| Metric | Target |
|---|---|
| Tasks visually grouped by week | 100% of tasks appear in correct week |
| Month navigation functional | All 3 buttons (prev/next/current) work |
| CRUD operations preserved | All 4 operations (C/R/U/D) remain functional |
| Existing filters work | Tag + completed toggles filter correctly |
| Page load time | < 2 seconds (no regression) |

## 9. Edge Cases / Constraints

| Case | Handling |
|---|---|
| Tasks spanning multiple weeks | Appear in all overlapping weeks (same as monthly-overview) |
| No tasks in selected month | Display empty state message |
| Tag filter applied | Filter tasks before/after week distribution |
| Completed tasks hidden | Apply filter to each week's task list |
| New task created | Pre-populate start date to first day of displayed month |
| Week crossing month boundary | Include if any days overlap with displayed month |
| User has no tags | Hide tag filter dropdown or show "No tags" |

## 10. Implementation Notes

### 10.1 Files Affected

| File | Change Type | Description |
|---|---|---|
| `frontend/src/app/features/tasks/tasks.component.ts` | Modify | Add week grouping logic, month navigation state |
| `frontend/src/app/features/tasks/tasks.component.html` | Modify | Restructure template with week sections |
| `frontend/src/app/features/tasks/tasks.component.css` | Modify | Add week header styling (reuse monthly-overview styles) |

### 10.2 Components / Services Involved
- `TasksComponent` - Main refactor target
- `TaskService` - May need `getTasksForMonth()` method
- Reuse patterns from `MonthlyOverviewComponent`

### 10.3 Implementation Steps
1. Add `WeekGroup` interface to tasks.component.ts
2. Add month navigation state properties
3. Port week calculation methods from monthly-overview
4. Modify `loadTasks()` to use date-filtered API or client-side filter
5. Update template to iterate over `weeksInMonth` instead of flat `tasks`
6. Add month navigation UI elements
7. Preserve existing task card markup inside week loops
8. Ensure filters (tag, completed) apply correctly to week-grouped data
9. Test CRUD operations maintain functionality

### 10.4 Reusable Assets from Monthly Overview
```typescript
// From monthly-overview.component.ts:7-12
interface WeekGroup { ... }

// From monthly-overview.component.ts:69-103
calculateWeeksInMonth(): WeekGroup[] { ... }

// From monthly-overview.component.ts:105-124
distributeTasksIntoWeeks(tasks, weeks): WeekGroup[] { ... }
```

### 10.5 Risk Mitigation
- **Risk:** Breaking existing CRUD flows
  - **Mitigation:** Keep modal and form handling completely unchanged
- **Risk:** Filter logic complexity
  - **Mitigation:** Apply filters at task level before week distribution
- **Risk:** Performance with large task lists
  - **Mitigation:** Server-side date filtering via `getTasksForMonth()`
