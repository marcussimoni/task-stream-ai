---
version: 1.0.0
last_updated: 2026-04-17
author: AI Assistant
---

# Monthly Overview Feature - Product Requirement Document

## 1. Feature Name
**Monthly Overview** (renamed from Weekly Overview)

## 2. Overview / Objective
Transform the existing Weekly Overview feature into a Monthly Overview view that displays tasks grouped by week within a selected month. The feature allows users to navigate between months and view all tasks that overlap with the selected month, organized chronologically by week.

## 3. Target Users
- End users tracking daily routines
- Users wanting a broader view of their monthly task distribution
- Users who need to plan tasks across longer time periods

## 4. Functional Requirements

### Core Functionality
- **Month Navigation**: Previous/Next/Current month navigation controls
- **Default View**: Always loads the current month on initial access
- **Date Range Query**: Fetch tasks where `startDate ≤ monthEnd` AND `endDate ≥ monthStart`
- **Week Grouping**: Tasks displayed in collapsible/expandable week sections
- **Sorting**: Tasks sorted alphabetically by name within each week group
- **Stats Summary**: Display total task count, completed count, and pending count

### Display Elements
- Month label showing selected month/year (e.g., "April 2026")
- Week sections labeled by date range (e.g., "Apr 1 - Apr 6", "Apr 7 - Apr 13")
- Task items showing: name, description, status badge, tag, date range, progress bar
- Empty state when no tasks for the selected month

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
- Implement week grouping algorithm:
  - Calculate ISO weeks within selected month
  - Distribute tasks into week buckets based on date overlap
- Update `TaskService` call: `getTasksForWeek()` → `getTasksForMonth()` (or reuse existing endpoint with date params)

### Backend/API (No DB changes required)
- **Option A**: Use existing `getTasksForWeek` endpoint - pass full month date range
- **Option B**: Create new `GET /api/tasks/monthly` endpoint accepting `monthStart` and `monthEnd` parameters

### Algorithm: Week Grouping
```typescript
// For a given month, calculate week buckets (Monday-Sunday)
// Assign each task to all weeks where it has at least one day overlap
// Sort tasks by name within each week
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
│  Week 1: Apr 1 - Apr 6              │
│  ┌─────────────────────────────┐     │
│  │ Task A    [In Progress]    │     │
│  │ Task B    [Completed]      │     │
│  └─────────────────────────────┘     │
│  Week 2: Apr 7 - Apr 13             │
│  ┌─────────────────────────────┐     │
│  │ Task C    [Not Started]      │     │
│  └─────────────────────────────┘     │
├─────────────────────────────────────┤
│  5 tasks | 2 completed | 3 pending  │
└─────────────────────────────────────┘
```

### Visual Requirements
- Week sections should be visually distinct (card-style or bordered)
- Tasks maintain existing styling: progress bar, status badge, tag color
- Week label shows short date range for that week

## 8. Success Metrics
- User can navigate to any month and see tasks within 1 second
- Tasks are correctly grouped into weeks based on date overlap
- Component renders without console errors
- All existing task display features preserved

## 9. Edge Cases / Constraints

### Edge Cases
- **Month spanning partial weeks**: First and last weeks may include days from previous/next month - include these partial weeks
- **Task spanning multiple weeks**: Task appears in each week it overlaps with
- **Empty month**: Display friendly empty state with hint text
- **Week boundaries**: Use ISO week standard (Monday as week start)

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
  weeksInMonth: WeekGroup[];
  loadTasksForMonth(): void;
}

interface WeekGroup {
  weekStart: Date;
  weekEnd: Date;
  weekLabel: string;
  tasks: Task[];
}
```

### API Endpoint (if needed)
```
GET /api/tasks?startDate={monthStart}&endDate={monthEnd}
Response: Task[] (same as current)
```
