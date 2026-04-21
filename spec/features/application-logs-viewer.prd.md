---
version: 1.0.0
last_updated: 2026-04-18
author: AI
---

# Application Logs Viewer - Product Requirements Document

## Overview

An admin panel feature that allows users to view, filter, and download backend application logs through a web interface. The feature provides a read-only log viewer with line count selection, log level filtering, text search, syntax highlighting for severity levels, and export functionality.

---

## Goals

1. Provide a centralized interface to view backend application logs
2. Enable filtering by log severity levels (ERROR, WARN, INFO, DEBUG, TRACE)
3. Support configurable line count display (100, 500, 1000, 5000)
4. Allow text search within the current log view
5. Provide log download/export functionality
6. Display logs with syntax highlighting matching severity levels
7. Ensure logs are displayed in chronological order (oldest to newest)

---

## Target Users

- All authenticated users accessing the Admin Panel section
- Developers and system administrators troubleshooting application issues
- Users needing to review application behavior and errors

---

## Functional Requirements

### 1. Log Retrieval

#### 1.1 Fetch Logs
- **Endpoint**: `GET /api/application-logs?lines={lines}`
- **Query Parameters**:
  - `lines` (optional): Number of log lines to retrieve (default: 500, options: 100, 500, 1000, 5000)
- **Response**: `LogDTO`
```json
{
  "title": "logs/application.log",
  "logs": [
    "2025-01-15 14:30:22.123 INFO  [main] o.s.b.StartupInfoLogger - Starting TaskStreamAiApplication",
    "2025-01-15 14:30:22.456 ERROR [http-nio-8080-exec-1] b.c.t.s.TaskService - Failed to create task",
    "2025-01-15 14:30:22.789 WARN  [http-nio-8080-exec-2] b.c.t.c.TaskController - Invalid task ID provided"
  ]
}
```

#### 1.2 Line Count Selection
- **UI Control**: Dropdown selector with options: 100, 500, 1000, 5000
- **Default Value**: 500 lines (matching backend default)
- **Action**: Changing selection triggers a new API request
- **Loading State**: Show loading indicator while fetching

### 2. Log Display

#### 2.1 Log Rendering
- Display logs in a scrollable, monospace text container
- Preserve whitespace and line breaks exactly as received
- Show logs in chronological order (oldest first, newest last)
- Each log line should be independently styled based on severity

#### 2.2 Severity Level Highlighting
Parse each log line to detect severity levels and apply color coding:

| Level | Color | CSS Class | Pattern Match |
|-------|-------|-----------|---------------|
| ERROR | Red | `log-level-error` | Contains " ERROR " |
| WARN | Yellow/Orange | `log-level-warn` | Contains " WARN " |
| INFO | Blue/Green | `log-level-info` | Contains " INFO " |
| DEBUG | Gray | `log-level-debug` | Contains " DEBUG " |
| TRACE | Light Gray | `log-level-trace` | Contains " TRACE " |

**Example Styling**:
```scss
.log-level-error { color: #dc2626; background: #fef2f2; }
.log-level-warn { color: #d97706; background: #fffbeb; }
.log-level-info { color: #059669; background: #ecfdf5; }
.log-level-debug { color: #6b7280; background: #f9fafb; }
.log-level-trace { color: #9ca3af; }
```

### 3. Filtering and Search

#### 3.1 Log Level Filter
- **UI Control**: Multi-select checkboxes or toggle buttons
- **Options**: ERROR, WARN, INFO, DEBUG, TRACE
- **Default**: All levels selected
- **Behavior**: 
  - Unchecking a level hides all lines containing that severity
  - Client-side filtering only (no backend re-request)
  - Apply filters immediately on selection change

#### 3.2 Text Search
- **UI Control**: Text input with clear button
- **Placeholder**: "Search in logs..."
- **Behavior**:
  - Real-time filtering as user types (debounced 300ms)
  - Case-insensitive search
  - Highlight matching text within lines
  - Show "No results" message when search yields no matches
  - Clear button resets search

#### 3.3 Combined Filters
- Log level filter and text search work together (AND logic)
- Both filters are client-side only

### 4. Download/Export

#### 4.1 Download Logs
- **UI Control**: "Download Logs" button with download icon
- **Action**: Downloads the currently retrieved log content as a text file
- **Filename**: `{title}_{timestamp}.log` (e.g., `application-logs_2025-01-15_143022.log`)
- **Content**: Raw log lines joined with newlines, exactly as received from API
- **Browser Behavior**: Trigger native file download

### 5. Admin Panel Section

#### 5.1 Navigation
- **Location**: Admin Panel section
- **Menu Item**: "Application Logs"
- **Icon**: Lucide `FileText` or `ScrollText` icon

#### 5.2 Layout
```
┌─────────────────────────────────────────────────────────┐
│  Application Logs                                         │
├─────────────────────────────────────────────────────────┤
│  Lines: [500 ▼]  [ERROR ✓] [WARN ✓] [INFO ✓] [↓ Download]│
│  ┌─────────────────────────────────────────────────────┐  │
│  │ Search in logs...                            [X]  │  │
│  └─────────────────────────────────────────────────────┘  │
├─────────────────────────────────────────────────────────┤
│  ┌─────────────────────────────────────────────────────┐  │
│  │ 2025-01-15 14:30:22.123 INFO  Starting applic...   │  │
│  │ 2025-01-15 14:30:22.456 ERROR Failed to create...  │  │
│  │ 2025-01-15 14:30:22.789 WARN  Invalid task ID...     │  │
│  │ ...                                                │  │
│  └─────────────────────────────────────────────────────┘  │
│                                                           │
│  Showing 3 of 500 lines (filtered)                         │
└─────────────────────────────────────────────────────────┘
```

#### 5.3 Status Bar
- Display current filter status: "Showing X of Y lines"
- When filters are active, show count of filtered vs total

---

## Non-Functional Requirements

### 1. Performance
- Log retrieval should complete within 3 seconds for 5000 lines
- Client-side filtering must be responsive (< 100ms for 5000 lines)
- Search debouncing to prevent excessive re-rendering
- Virtual scrolling for large log sets (optional optimization)

### 2. Security
- All endpoints require authentication (existing JWT)
- Read-only access (no log modification capabilities)
- No server-side log deletion through this interface

### 3. Usability
- Clear visual hierarchy with severity colors
- Monospace font for consistent log alignment
- Scrollbar indication for overflow content
- Empty state when no logs match filters
- Error state with retry option if API fails

---

## Technical Requirements

### Architecture
```
frontend/src/app/
├── features/
│   └── admin/
│       ├── components/
│       │   └── application-logs/
│       │       ├── application-logs.component.ts
│       │       ├── application-logs.component.html
│       │       └── application-logs.component.scss
│       ├── models/
│       │   └── log.model.ts
│       └── services/
│           └── log.service.ts
```

### Models

#### LogDTO
```typescript
export interface LogDTO {
  title: string;
  logs: string[];
}
```

#### LogLevel (Enum)
```typescript
export enum LogLevel {
  ERROR = 'ERROR',
  WARN = 'WARN',
  INFO = 'INFO',
  DEBUG = 'DEBUG',
  TRACE = 'TRACE'
}
```

#### LogFilterState
```typescript
export interface LogFilterState {
  lines: number;
  selectedLevels: LogLevel[];
  searchTerm: string;
}
```

### Service Design

#### LogService
```typescript
@Injectable({ providedIn: 'root' })
export class LogService {
  private http = inject(HttpClient);
  private apiUrl = '/api/application-logs';

  getLogs(lines: number = 500): Observable<LogDTO> {
    return this.http.get<LogDTO>(`${this.apiUrl}?lines=${lines}`);
  }
}
```

### Component Design

#### ApplicationLogsComponent
- **Standalone Angular 21 component**
- **Signals for state management**:
  - `logs`: Signal<string[]> - all retrieved log lines
  - `filteredLogs`: Signal<string[]> - filtered view
  - `title`: Signal<string> - log file title
  - `loading`: Signal<boolean> - API loading state
  - `lines`: Signal<number> - selected line count (default: 500)
  - `selectedLevels`: Signal<Set<LogLevel>> - active level filters
  - `searchTerm`: Signal<string> - search query

- **Computed signals**:
  - `filteredLogs`: Derived from logs + selectedLevels + searchTerm

- **Methods**:
  - `loadLogs()`: Fetch logs from API with current line count
  - `onLinesChange(count: number)`: Update line count and reload
  - `toggleLevel(level: LogLevel)`: Toggle level filter
  - `onSearch(term: string)`: Update search filter
  - `clearSearch()`: Reset search term
  - `downloadLogs()`: Trigger file download of current logs
  - `detectLogLevel(line: string): LogLevel | null`: Parse severity from line

### UI Components
- **Dropdown**: Line count selector (100, 500, 1000, 5000)
- **Toggle Chips**: Log level filter buttons (ERROR, WARN, INFO, DEBUG, TRACE)
- **Search Input**: Text search with clear button
- **Button**: Download logs (primary outlined)
- **Log Container**: Scrollable div with monospace font
- **Status Bar**: Filter result count display

### Utility Functions

#### Log Level Detection
```typescript
function detectLogLevel(line: string): LogLevel | null {
  if (line.includes(' ERROR ')) return LogLevel.ERROR;
  if (line.includes(' WARN ')) return LogLevel.WARN;
  if (line.includes(' INFO ')) return LogLevel.INFO;
  if (line.includes(' DEBUG ')) return LogLevel.DEBUG;
  if (line.includes(' TRACE ')) return LogLevel.TRACE;
  return null;
}
```

#### Client-Side Filtering
```typescript
function filterLogs(
  logs: string[],
  selectedLevels: Set<LogLevel>,
  searchTerm: string
): string[] {
  return logs.filter(line => {
    // Level filter
    if (selectedLevels.size > 0) {
      const level = detectLogLevel(line);
      if (level && !selectedLevels.has(level)) return false;
    }
    
    // Search filter
    if (searchTerm && !line.toLowerCase().includes(searchTerm.toLowerCase())) {
      return false;
    }
    
    return true;
  });
}
```

#### File Download
```typescript
function downloadLogs(title: string, logs: string[]): void {
  const content = logs.join('\n');
  const blob = new Blob([content], { type: 'text/plain' });
  const url = window.URL.createObjectURL(blob);
  const link = document.createElement('a');
  const timestamp = new Date().toISOString().replace(/[:.]/g, '-').slice(0, 19);
  
  link.href = url;
  link.download = `${title.replace(/\//g, '_')}_${timestamp}.log`;
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  window.URL.revokeObjectURL(url);
}
```

---

## UI / UX Specifications

### Page Layout
```
┌─────────────────────────────────────────────────────────┐
│  Application Logs                                         │
├─────────────────────────────────────────────────────────┤
│  Toolbar                                                  │
│  ┌───────────┐  ┌─────┐ ┌─────┐ ┌─────┐ ┌─────┐  ┌────┐  │
│  │ Lines: 500│  │ERROR│ │WARN │ │INFO │ │DEBUG│  │ ↓  │  │
│  │     ▼     │  │ (3) │ │ (5) │ │(492)│ │ (0) │  │Save│  │
│  └───────────┘  └─────┘ └─────┘ └─────┘ └─────┘  └────┘  │
├─────────────────────────────────────────────────────────┤
│  Search Bar                                               │
│  ┌─────────────────────────────────────────────────────┐  │
│  │ 🔍 Search in logs...                           [X]  │  │
│  └─────────────────────────────────────────────────────┘  │
├─────────────────────────────────────────────────────────┤
│  Log Viewer                                               │
│  ┌─────────────────────────────────────────────────────┐  │
│  │ 2025-01-15 10:00:01 INFO  Application started      │  │
│  │ 2025-01-15 10:05:23 WARN  Slow query detected       │  │
│  │ 2025-01-15 10:12:45 ERROR Database connection fail │  │
│  │ 2025-01-15 10:12:46 INFO  Retrying connection...    │  │
│  │ ...                                                │  │
│  │                                                    │  │
│  └─────────────────────────────────────────────────────┘  │
├─────────────────────────────────────────────────────────┤
│  Status: Showing 492 of 500 lines (8 hidden by filters)   │
└─────────────────────────────────────────────────────────┘
```

### Color Scheme
- **Background**: `$surface-color` or white
- **Log Container**: Light gray background (#f8f9fa)
- **Log Lines**: Monospace font (Consolas, Monaco, Courier)
- **Level Colors**:
  - ERROR: Red text (#dc2626) on light red background (#fef2f2)
  - WARN: Orange text (#d97706) on light orange background (#fffbeb)
  - INFO: Green text (#059669) on light green background (#ecfdf5)
  - DEBUG: Gray text (#6b7280)
  - TRACE: Light gray text (#9ca3af)

### Responsive Behavior
- **Desktop**: Full layout with all filters inline
- **Tablet**: Filters wrap to two rows if needed
- **Mobile**: 
  - Line selector and download on top row
  - Level filters on second row (scrollable horizontal)
  - Log viewer takes full remaining height

---

## Success Metrics

- Users can view logs with configurable line counts
- Log level highlighting displays correctly for all severity levels
- Level filters correctly hide/show matching log lines
- Text search filters logs in real-time
- Download button saves logs as a .log file
- Page displays filtered count vs total count accurately
- Logs load within 3 seconds for 5000 lines
- No UI blocking during log operations
- All existing tests pass

---

## Edge Cases / Constraints

### Edge Cases
1. **Empty Log File**: Display message "No log entries found"
2. **API Failure**: Show error toast with retry button
3. **All Filters Disabled**: Show warning "Select at least one log level"
4. **Search No Results**: Display "No logs match your search"
5. **Very Long Lines**: Horizontal scroll in log container, preserve line integrity
6. **Special Characters**: Properly escape HTML in log lines to prevent XSS
7. **Large Log Sets**: Consider virtual scrolling if performance degrades

### Constraints
- No auto-refresh functionality (manual reload only)
- Client-side filtering only (no backend search/filter)
- Read-only access (no log editing or deletion)
- Log level detection based on pattern matching (may not be 100% accurate for custom formats)

---

## Implementation Notes

### Files Affected

#### New Files
- `frontend/src/app/features/admin/components/application-logs/application-logs.component.ts`
- `frontend/src/app/features/admin/components/application-logs/application-logs.component.html`
- `frontend/src/app/features/admin/components/application-logs/application-logs.component.scss`
- `frontend/src/app/features/admin/models/log.model.ts`
- `frontend/src/app/features/admin/services/log.service.ts`

#### Modified Files
- Admin panel routing module (add new route for `/admin/logs`)
- Admin panel navigation (add "Application Logs" menu item)

### Backend Dependencies
- `@/src/main/kotlin/br/com/taskstreamai/controller/ApplicationLogController.kt`
- Endpoint: `GET /api/application-logs?lines={lines}`
- DTO: `LogDTO` (`title: String`, `logs: List<String>`)

### Integration Points
- Toast notification service (for API errors)
- Authentication/authorization (existing JWT)
- HTTP client (Angular HttpClient)

### Testing Requirements

#### Unit Tests
- **LogService**: HTTP GET with lines parameter, error handling
- **ApplicationLogsComponent**:
  - State initialization with default values
  - `loadLogs()` triggers API call
  - `detectLogLevel()` correctly identifies severity
  - `filterLogs()` applies level and search filters
  - `downloadLogs()` creates correct blob and filename
  - Toggle level adds/removes from filter set
  - Search term updates trigger re-filtering

#### E2E Tests
- Navigate to Application Logs page
- Verify default 500 lines loaded
- Change line count and verify new request
- Toggle log level filters and verify visibility
- Enter search term and verify filtering
- Click download and verify file download
- Verify error handling with toast notification

---

## Acceptance Criteria

- [ ] Admin Panel has "Application Logs" menu item with FileText icon
- [ ] Page displays line count dropdown with options 100, 500, 1000, 5000
- [ ] Default line count is 500 on initial load
- [ ] Log level toggle buttons show ERROR, WARN, INFO, DEBUG, TRACE
- [ ] All log levels are selected by default
- [ ] Log lines display with color coding matching severity level
- [ ] Search input filters logs in real-time (case-insensitive)
- [ ] Clear button resets search field
- [ ] Download button saves logs as `{title}_{timestamp}.log`
- [ ] Status bar shows "Showing X of Y lines" with filter counts
- [ ] Empty state displays when logs are empty
- [ ] Error state displays with retry option on API failure
- [ ] Level filter badge shows count per level (e.g., "ERROR (3)")
- [ ] Page is responsive on tablet and mobile devices
- [ ] No auto-refresh functionality is implemented
- [ ] All existing tests pass
