---
version: 1.0.0
last_updated: 2026-04-17
author: AI Assistant
---

# Task Metrics Dashboard - Product Requirements Document

## Overview

The Task Metrics Dashboard provides a visual representation of task completion data organized by tags. It displays monthly task statistics using a bar chart that shows completed vs pending tasks for each tag, helping users track their productivity and identify areas needing attention.

## Goals

1. Provide visual insights into task completion patterns by tag
2. Help users identify which task categories have pending work
3. Enable month-by-month comparison of task productivity
4. Offer a quick overview of total completed vs incomplete tasks

## Target Users

- Users who want to track their task completion progress visually
- Users managing tasks across multiple categories/tags
- Users who need monthly productivity summaries

## Functional Requirements

### 1. Date Selection

Users can select a specific month and year to view task metrics:
- **Month selector**: Dropdown with 12 months (January - December)
- **Year selector**: Number input (range: 2020-2030)
- Metrics automatically update when date selection changes

### 2. Chart Display

**Chart Type**: Horizontal bar chart using Chart.js library

**Data Visualization**:
- **X-axis**: Task tags (e.g., "Work", "Study", "Personal")
- **Y-axis**: Number of tasks
- **Bars**: Two datasets per tag
  - Green bars: Completed tasks count
  - Red bars: Pending/Incomplete tasks count

**Chart Features**:
- Chart title: "Tasks by Tag"
- Legend showing total completed and incomplete counts
- Responsive design that adapts to container width
- Non-stacked bars for clear comparison

### 3. Data States

**With Data**:
- Chart renders when tasks exist for the selected month
- Each tag with tasks appears as a category on the chart

**Empty State**:
- When no task data exists for the selected month, display:
  - "No task data available for the selected month."
  - "Start adding tasks to see metrics here!"

## Non-Functional Requirements

### Performance
- Chart renders within 1 second of data load
- Smooth transitions when switching months

### Browser Support
- Modern browsers (Chrome, Firefox, Safari, Edge)
- Chart.js library handles cross-browser canvas rendering

## Technical Requirements

### Frontend Architecture (Angular)

```
frontend/src/app/
├── features/
│   └── metrics/
│       ├── metrics.component.ts       # Component logic
│       ├── metrics.component.html     # Template
│       └── metrics.component.css      # Styles
├── core/
│   ├── services/
│   │   └── metrics.service.ts         # API calls
│   └── models/
│       └── metrics.model.ts           # TypeScript interfaces
```

#### Component (`metrics.component.ts`)

```typescript
export class MetricsComponent {
  taskMetrics: MonthlyTasksMetrics;      // Data from API
  metricsForm: FormGroup;               // Month/year controls
  
  loadTaskMetrics(): void;              // Fetch data
  createTaskChart(): void;             // Render Chart.js bar chart
  getTaskMetricsByTag(): GroupedData;   // Group by tag with counts
}
```

#### Service (`metrics.service.ts`)

```typescript
export class MetricsService {
  getMonthlyTaskMetrics(month: number, year: number): Observable<MonthlyTasksMetrics>
  // Endpoint: GET /api/metrics/monthly/tasks?month={month}&year={year}
}
```

#### Data Models (`metrics.model.ts`)

```typescript
export interface TaskMetricsItem {
  tagId: number;
  tag: string;
  total: number;
  status: 'completed' | 'incompleted';
  date: string;
}

export interface MonthlyTasksMetrics {
  tasksMetrics: Array<TaskMetricsItem>;
  totalCompleted: number;
  totalIncomplete: number;
}
```

### Backend Architecture (Spring Boot + Kotlin)

```
src/main/kotlin/br/com/dailytrack/
├── controller/
│   └── MetricsController.kt
├── service/
│   └── MetricsService.kt
├── dto/
│   └── MonthlyTaskMetricsDTO.kt
└── repository/
    └── TaskRepository.kt
```

#### API Endpoint

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/metrics/monthly/tasks?month={month}&year={year}` | Get monthly task metrics grouped by tag |

#### Response Format

```json
{
  "tasksMetrics": [
    { "tagId": 1, "tag": "Work", "total": 5, "status": "completed", "date": "2026-04-01" },
    { "tagId": 1, "tag": "Work", "total": 2, "status": "incompleted", "date": "2026-04-02" }
  ],
  "totalCompleted": 15,
  "totalIncomplete": 8
}
```

Note: Frontend groups these by tag and sums totals by status for chart display.

## UI/UX Specifications

### Layout

```
┌─────────────────────────────────────────────┐
│          Metrics Dashboard                  │
├─────────────────────────────────────────────┤
│  Month: [Dropdown]  Year: [Input]           │
├─────────────────────────────────────────────┤
│                                             │
│     ┌─────────────────────────────────┐     │
│     │                                 │     │
│     │     Tasks by Tag (Bar Chart)    │     │
│     │                                 │     │
│     │  Work    [███████][███]         │     │
│     │  Study   [████][██████]         │     │
│     │  Personal[██][█████████]          │     │
│     │                                 │     │
│     │  Completed: 15 | Pending: 8     │     │
│     │                                 │     │
│     └─────────────────────────────────┘     │
│                                             │
└─────────────────────────────────────────────┘
```

### Design Details

- **Container**: Card layout with padding
- **Controls**: Inline month/year selectors with labels
- **Chart**: Responsive canvas with Chart.js styling
- **Colors**:
  - Completed: `#28a745` (green)
  - Pending: `#dc3545` (red)
- **Empty State**: Centered muted text with helper message

## Success Metrics

- **Functional**: Chart renders correctly for any month with task data
- **Usability**: Users can easily identify which tags have pending tasks at a glance
- **Accuracy**: Completed/pending counts match actual task data in database
- **Performance**: Page loads and chart renders within 2 seconds

## Edge Cases & Constraints

### Data Scenarios

**No Tasks for Selected Month**: Display empty state message, no chart rendered

**Single Tag with Tasks**: Chart displays one category

**All Tasks Completed**: Only green bars visible, legend shows 0 incomplete

**All Tasks Pending**: Only red bars visible, legend shows 0 completed

**Large Number of Tags**: Chart.js handles horizontal scrolling/scaling automatically

### Technical Constraints

- Chart.js library required for rendering
- Canvas element must be visible in DOM before chart creation
- Date range limited to years 2020-2030

## Implementation Notes

### Files Affected

| File | Purpose |
|------|---------|
| `frontend/src/app/features/metrics/metrics.component.ts` | Component logic, chart creation, data grouping |
| `frontend/src/app/features/metrics/metrics.component.html` | Template with controls and chart container |
| `frontend/src/app/features/metrics/metrics.component.css` | Component styles |
| `frontend/src/app/core/services/metrics.service.ts` | API service for metrics data |
| `frontend/src/app/core/models/metrics.model.ts` | TypeScript interfaces |
| `src/main/kotlin/br/com/dailytrack/controller/MetricsController.kt` | REST endpoint |
| `src/main/kotlin/br/com/dailytrack/service/MetricsService.kt` | Business logic |
| `src/main/kotlin/br/com/dailytrack/dto/MonthlyTaskMetricsDTO.kt` | Data transfer objects |

### Dependencies

- **Frontend**: Chart.js (bar chart visualization), Angular ReactiveForms
- **Backend**: Spring Boot, Kotlin, JPA Repository for task aggregation queries

### Chart.js Integration

The component uses Chart.js library for rendering the bar chart. Key configuration:
- Bar chart type with side-by-side bars (non-stacked)
- Two datasets: completed and incomplete counts
- Responsive sizing with maintainAspectRatio: false
- Legend positioned at top showing totals
