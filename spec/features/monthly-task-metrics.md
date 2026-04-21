# Product Requirement Document (PRD) - Task Metrics Feature

## 1. Feature Overview
**Feature Name:** Monthly Task Metrics  
**Description:** A dashboard that provides a summary of completed and pending tasks for users.  
**Objective:** To give users a clear overview of their tasks for a selected month, allowing them to monitor progress efficiently.

## 2. Target Users
- All users with access to the system.

## 3. Functional Requirements
- Display a summary of tasks for a specific month.
- Implement tabs to organize dashboards:
  - Existing dashboards → **Others** tab
  - Task Metrics → **Task** tab
- The layout should follow the existing design guidelines of the current dashboards.

## 4. User Interface / UX
- The feature is implemented at `track-daily-routine/frontend/src/app/features/metrics/metrics.component.html`
- Tabs allow seamless switching between **Tasks** and **Others** dashboards.
- **Tasks Tab:**
  - Month and year selectors (default: current month/year)
  - **Bar chart displaying tasks grouped by tag**
    - Vertical bars with grouped display (completed and pending side-by-side)
    - Green bars (#28a745) for completed tasks
    - Red bars (#dc3545) for pending tasks
    - X-axis: Tag names
    - Y-axis: Task count
    - Responsive design that fills available width
- **Others Tab:**
  - Contains all existing study metrics (weekly/monthly toggle, study time, sessions, habits, achievements)
- Display all data returned by the backend without filtering.

## 5. Technical Requirements
- Data is retrieved from the backend API.
  - **API Endpoint:** `/api/metrics/monthly/tasks?month=4&year=2026`
  - **Expected Response:**
```json
[{"tagId":6,"tag":"Health","total":1,"status":"completed","date":"2026-04-01"},{"tagId":6,"tag":"Health","total":1,"status":"incompleted","date":"2026-04-02"},{"tagId":14,"tag":"Music","total":2,"status":"completed","date":"2026-04-02"},{"tagId":1,"tag":"Study","total":1,"status":"completed","date":"2026-04-02"},{"tagId":4,"tag":"Work","total":1,"status":"completed","date":"2026-04-02"}]
```
- Frontend Implementation:
  - **Service:** `MetricsService.getMonthlyTaskMetrics(month, year)` at `track-daily-routine/frontend/src/app/core/services/metrics.service.ts`
  - **Component:** `MetricsComponent` at `track-daily-routine/frontend/src/app/features/metrics/metrics.component.ts`
  - **Model:** `TaskMetricsItem` interface at `track-daily-routine/frontend/src/app/core/models/metrics.model.ts`
- Data is grouped by tag on the frontend to show aggregated completed/pending counts per tag.
- Compatible with the existing platform layout.
- No additional performance or security constraints beyond existing system requirements.

## 6. Metrics and Success Criteria
- Success is measured by:
  - Correct display of all task data from the backend.
  - Tabs functionality works as expected with seamless switching.
  - Tasks tab shows month/year selectors with current month/year as default.
  - Task metrics are displayed as a **grouped bar chart** showing completed vs pending tasks by tag.
  - Chart uses **ng2-charts** library with Chart.js for responsive visualization.
  - Chart displays green bars for completed tasks and red bars for pending tasks.
  - Build completes successfully without errors.

## 7. Edge Cases and Constraints
- Empty task data state: Display friendly message when no tasks exist for selected month.
- Month/year selectors must default to current month and year on initial load.
- Tab switching should trigger data refresh for Tasks tab.
- Implementation follows existing system constraints.

## 8. Implementation Notes
- **Files Modified:**
  - `track-daily-routine/frontend/src/app/core/models/metrics.model.ts` - Added `TaskMetricsItem` interface
  - `track-daily-routine/frontend/src/app/core/services/metrics.service.ts` - Added `getMonthlyTaskMetrics()` method
  - `track-daily-routine/frontend/src/app/features/metrics/metrics.component.ts` - Added task metrics state, chart configuration, `updateChartData()` method, and `getTaskMetricsByTag()` grouping function
  - `track-daily-routine/frontend/src/app/features/metrics/metrics.component.html` - Added Tasks/Others tabs with bar chart visualization for task metrics
  - `track-daily-routine/frontend/src/app/features/metrics/metrics.component.css` - Added styles for dashboard tabs, chart container, and responsive design


