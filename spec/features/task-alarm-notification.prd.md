---
version: 1.0.0
last_updated: 2026-04-11
author: AI Assistant
---

# Task Alarm Notification - Product Requirements Document

## Overview

A real-time task alarm notification system that alerts users when it's time to start a scheduled task. Similar to a smartphone alarm clock, the system provides:
- **Pre-task reminder**: Alert 5 minutes before task start time (e.g., 3:55 PM for a 4:00 PM task)
- **Task start alarm**: Alert exactly when the task should begin (e.g., 4:00 PM)

The backend calculates alarm times based on the weekly calendar schedule and pushes notifications to the frontend via Server-Sent Events (SSE). The frontend displays visual notifications.

---

## Goals

1. Help users never miss scheduled tasks through visual alarms
2. Provide timely pre-task reminders for preparation (fixed: 5 minutes before)
3. Use persistent SSE connection for real-time, server-push notifications
4. Ensure alarms work across browser tabs, page reloads, and all app pages
5. Auto-navigate to weekly calendar and show task details when alarm fires
6. Allow users to dismiss alarms (alarms persist until acknowledged)

---

## Target Users

- Users who schedule tasks using the weekly calendar
- Users who need visual reminders for time-sensitive activities
- Users who want proactive notifications without checking the app

---

## Functional Requirements

### 1. Alarm Types

| Alarm Type | Timing | Purpose |
|------------|--------|---------|
| Pre-task Reminder | Fixed: 5 min before task start | Preparation warning |
| Task Start Alarm | Exact task start time | Begin task now |

### 2. Notification Content

```typescript
interface TaskAlarm {
  id: string;                    // Unique alarm instance ID
  type: 'PRE_REMINDER' | 'START_ALARM';
  scheduleId: number;            // Reference to WeekSchedule
  tagName: string;               // e.g., "Work", "Study"
  tagColor: string;              // Hex color for visual styling
  taskName?: string;             // Associated task name if exists
  scheduledTime: string;         // ISO datetime (e.g., "2026-04-11T16:00:00")
  message: string;               // Human-readable message
}
```

**Message Examples:**
- Pre-reminder: `"Your Work session starts in 5 minutes (4:00 PM)"`
- Start alarm: `"START NOW: Work session at 4:00 PM"`

### 3. Alarm Behavior

**Pre-task Reminder (3:55 PM for 4:00 PM task) - Fixed 5 minutes:**
- Single notification
- Auto-dismisses after 10 seconds
- Visual: Toast notification with tag color
- Can be dismissed manually
- **Pre-reminder time is fixed at 5 minutes (not configurable)**

**Task Start Alarm (4:00 PM exactly):**
- Persistent notification
- **Auto-navigates to weekly calendar view** when triggered
- **Highlights the scheduled cell** with task/tag information
- Visual: Modal overlay blocking interaction
- Options: "Start Task", "Dismiss"
- Tracks acknowledgment state

### 4. SSE Connection Management

- Connection endpoint: `GET /api/alarms/stream`
- Heartbeat: Every 30 seconds to keep connection alive
- Auto-reconnect: Exponential backoff (1s, 2s, 4s, 8s, max 30s)
- Connection state indicator in UI
- Re-subscribe on page load with `Last-Event-ID` header

---

## Non-Functional Requirements

### Performance
- SSE latency: < 1 second from alarm time to notification
- Backend alarm scheduler: Only 24 checks/day (runs at :55 and :00 each hour during 12h operation)
- Support for 100+ concurrent SSE connections

### Reliability
- Alarms survive browser refresh (server tracks pending alarms)
- Reconnection within 60 seconds: no missed alarms
- Reconnection after 60 seconds: sync missed alarms

### Browser Support
- Modern browsers with SSE support (Chrome, Firefox, Safari, Edge)

---

## Technical Requirements

### Backend Architecture (Spring Boot + Kotlin)

```
src/main/kotlin/br/com/dailytrack/
├── controller/
│   └── AlarmController.kt           # SSE endpoint
├── service/
│   ├── PreReminderScheduler.kt      # Runs at :55 every hour
│   ├── StartAlarmScheduler.kt       # Runs at :00 every hour
│   ├── AlarmEmitterService.kt       # SSE emitter management
│   └── WeekScheduleService.kt       # Existing, modified for alarms
├── model/
│   └── PendingAlarm.kt              # Tracks active/pending alarms
├── repository/
│   └── PendingAlarmRepository.kt
└── dto/
    └── AlarmDTO.kt
```

#### SSE Endpoint

```kotlin
@RestController
@RequestMapping("/api/alarms")
class AlarmController(
    private val alarmEmitterService: AlarmEmitterService
) {
    @GetMapping("/stream", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun streamAlarms(
        @RequestHeader(value = "Last-Event-ID", required = false) lastEventIdHeader: String?,
        @RequestParam(value = "lastEventId", required = false) lastEventIdParam: String?
    ): SseEmitter {
        val lastEventId = lastEventIdHeader ?: lastEventIdParam
        // Returns SseEmitter with heartbeat and reconnection support
    }
}
```

#### SSE Exception Handling

To handle client disconnections gracefully and prevent "Broken pipe" errors:

```kotlin
// GlobalExceptionHandler.kt
@ControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(AsyncRequestNotUsableException::class)
    fun handleAsyncRequestNotUsable(ex: AsyncRequestNotUsableException): ResponseEntity<Void> {
        // Client disconnected from SSE - return 204 No Content
        return ResponseEntity.noContent().build()
    }
}

// AlarmEmitterService.kt - Wrap all emitter operations in try-catch
private fun broadcastAlarm(alarmDTO: AlarmDTO) {
    try {
        val deadEmitters = mutableListOf<SseEmitter>()
        emitters.forEach { emitter ->
            try {
                emitter.send(SseEmitter.event()
                    .id(alarmDTO.id)
                    .name("alarm")  // Named events for frontend
                    .data(alarmDTO))
            } catch (e: Throwable) {  // Catch Throwable, not just Exception
                deadEmitters.add(emitter)
            }
        }
        deadEmitters.forEach { emitters.remove(it) }
    } catch (e: Throwable) {
        logger.error("Unexpected error in broadcastAlarm", e)
    }
}
```

**Key implementation details:**
- Use named SSE events (`.name("alarm")`, `.name("heartbeat")`) instead of default message events
- Catch `Throwable` not `Exception` to handle all error types including `IOException: Broken pipe`
- Return `204 No Content` for `AsyncRequestNotUsableException` to avoid content-type mismatch
- Frontend uses `addEventListener('alarm', ...)` not `onmessage` for named events

#### Optimized Alarm Schedulers (Two Triggers)

Since tasks are scheduled hourly, we use two efficient cron triggers instead of polling:

```kotlin
@Service
class PreReminderScheduler(
    private val weekScheduleService: WeekScheduleService,
    private val alarmEmitterService: AlarmEmitterService
) {
    // Runs at minute 55 of every hour (e.g., 8:55, 9:55, 10:55)
    // Fixed 5-minute pre-reminder: tasks starting at :00 get reminder now
    @Scheduled(cron = "0 55 * * * *")
    fun emitPreReminders() {
        val targetHour = LocalDateTime.now().plusMinutes(5)
        
        val preReminders = weekScheduleService.findSchedulesStartingAt(
            hour = targetHour.hour,
            date = targetHour.toLocalDate()
        )
        
        preReminders.forEach { schedule ->
            alarmEmitterService.emitPreReminder(schedule)
        }
    }
}

@Service
class StartAlarmScheduler(
    private val weekScheduleService: WeekScheduleService,
    private val alarmEmitterService: AlarmEmitterService
) {
    // Runs at minute 0 of every hour (e.g., 9:00, 10:00, 11:00)
    @Scheduled(cron = "0 0 * * * *")
    fun emitStartAlarms() {
        val now = LocalDateTime.now()
        
        val startAlarms = weekScheduleService.findSchedulesStartingAt(
            hour = now.hour,
            date = now.toLocalDate()
        )
        
        startAlarms.forEach { schedule ->
            alarmEmitterService.emitStartAlarm(schedule)
        }
    }
}
```

#### Data Models

```kotlin
// PendingAlarm.kt - Tracks alarm state for resilience
data class PendingAlarm(
    val id: String = UUID.randomUUID().toString(),
    val scheduleId: Long,
    val type: AlarmType,
    val scheduledTime: LocalDateTime,
    val emittedAt: LocalDateTime? = null,
    val acknowledgedAt: LocalDateTime? = null
)

enum class AlarmType { PRE_REMINDER, START_ALARM }

// AlarmDTO.kt - SSE payload
data class AlarmDTO(
    val id: String,
    val type: String, // "PRE_REMINDER" or "START_ALARM"
    val scheduleId: Long,
    val tagName: String,
    val tagColor: String,
    val taskName: String?,
    val scheduledTime: String, // ISO format
    val message: String
)
```

#### Database Schema Addition

```sql
CREATE TABLE pending_alarms (
    id VARCHAR(36) PRIMARY KEY,
    schedule_id BIGINT NOT NULL,
    type VARCHAR(20) NOT NULL,
    scheduled_time TIMESTAMP NOT NULL,
    emitted_at TIMESTAMP,
    acknowledged_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (schedule_id) REFERENCES week_schedules(id)
);

CREATE INDEX idx_pending_alarms_time ON pending_alarms(scheduled_time);
CREATE INDEX idx_pending_alarms_acknowledged ON pending_alarms(acknowledged_at) WHERE acknowledged_at IS NULL;
```

### Frontend Architecture (Angular)

```
frontend/src/app/
├── features/
│   └── weekly-calendar/
│       └── weekly-calendar.component.ts    # Modified: highlight alarm cell
├── core/
│   └── services/
│       └── alarm.service.ts                # SSE client, audio, navigation
│   └── models/
│       └── alarm.model.ts
└── shared/
    └── components/
        └── alarm-modal/
            ├── alarm-modal.component.ts    # Start alarm modal
            ├── alarm-modal.component.html
            └── alarm-modal.component.scss
```

#### Alarm Service (Angular)

```typescript
// alarm.model.ts
export interface TaskAlarm {
  id: string;
  type: 'PRE_REMINDER' | 'START_ALARM';
  scheduleId: number;
  tagName: string;
  tagColor: string;
  taskName?: string;
  scheduledTime: string;
  message: string;
}

// alarm.service.ts
@Injectable({ providedIn: 'root' })
export class AlarmService {
  private currentAlarm = signal<TaskAlarm | null>(null);
  readonly activeAlarm = this.currentAlarm.asReadonly();
  
  private eventSource: EventSource | null = null;
  private chimeAudio: HTMLAudioElement | null = null;
  private alarmInterval: any = null;
  
  constructor(private router: Router, private http: HttpClient) {}
  
  // SSE connection management
  connect(): void {
    // Support reconnect with lastEventId via query param (EventSource can't set headers)
    const url = this.lastEventId
      ? `/api/alarms/stream?lastEventId=${encodeURIComponent(this.lastEventId)}`
      : '/api/alarms/stream';
    
    this.eventSource = new EventSource(url);
    
    // Use named event listeners for SSE events (not onmessage)
    this.eventSource.addEventListener('heartbeat', (event) => {
      this.lastEventId = event.lastEventId || null;
    });
    
    this.eventSource.addEventListener('alarm', (event) => {
      this.lastEventId = event.lastEventId || null;
      const alarm: TaskAlarm = JSON.parse(event.data);
      this.handleAlarm(alarm);
    });
    
    this.eventSource.onerror = () => {
      // Auto-reconnect with backoff (browser handles initial reconnects)
      if (this.reconnectAttempts > 3) {
        setTimeout(() => this.connect(), this.getBackoffDelay());
      }
      this.reconnectAttempts++;
    };
  }
  
  // Handle incoming alarms
  private handleAlarm(alarm: TaskAlarm): void {
    if (alarm.type === 'PRE_REMINDER') {
      this.showToast(alarm);
    } else {
      this.currentAlarm.set(alarm);
      // Auto-navigate to weekly calendar and highlight the cell
      this.router.navigate(['/weekly-calendar']).then(() => {
        // Signal to calendar component to highlight the specific cell
        this.highlightAlarmCell(alarm);
      });
    }
  }

  private highlightAlarmCell(alarm: TaskAlarm): void {
    // Broadcast to calendar component via subject or signal
  }

  // User actions
  acknowledge(): void {
    this.http.post(`/api/alarms/${this.currentAlarm()?.id}/acknowledge`, {}).subscribe();
    this.currentAlarm.set(null);
  }
  dismiss(): void {
    this.currentAlarm.set(null);
  }
}
```

#### Alarm Modal Component

```html
<!-- alarm-modal.component.html -->
<div class="alarm-modal-overlay" *ngIf="alarm()">
  <div class="alarm-modal" [style.border-color]="alarm()?.tagColor">
    <div class="alarm-header" [style.background-color]="alarm()?.tagColor + '20'">
      <span class="alarm-icon">⏰</span>
      <h2>{{ alarm()?.type === 'START_ALARM' ? 'TIME TO START!' : 'Reminder' }}</h2>
    </div>
    
    <div class="alarm-body">
      <h3>{{ alarm()?.tagName }}</h3>
      <!-- Task name displayed prominently -->
      <p class="task-name" *ngIf="alarm()?.taskName">
        <strong>Task:</strong> {{ alarm()?.taskName }}
      </p>
      <p class="alarm-message">{{ alarm()?.message }}</p>
      <p class="scheduled-time">{{ alarm()?.scheduledTime | date:'shortTime' }}</p>
    </div>
    
    <div class="alarm-actions">
      <button class="btn-primary" (click)="onStart()">
        {{ alarm()?.type === 'START_ALARM' ? 'Start Task' : 'OK' }}
      </button>
      <button class="btn-dismiss" (click)="onDismiss()">Dismiss</button>
    </div>
  </div>
</div>
```

#### Integration with WeeklyCalendarComponent

```typescript
// Add to WeeklyCalendarComponent
export class WeeklyCalendarComponent implements OnInit {
  private alarmService = inject(AlarmService);
  
  ngOnInit(): void {
    this.loadTags();
    this.loadWeekSchedule();
    this.alarmService.connect(); // Start SSE connection
  }
}
```

```html
<!-- Add to weekly-calendar.component.html, after loading overlay -->
<app-alarm-modal 
  [alarm]="alarmService.activeAlarm()"
  (acknowledge)="alarmService.acknowledge()"
  (dismiss)="alarmService.dismiss()">
</app-alarm-modal>
```

---

## UI/UX Specifications

### Alarm Modal Design

**Pre-reminder Toast (top-right, auto-dismiss):**
- Background: Tag color at 10% opacity
- Border-left: 4px solid tag color
- Icon: Bell/chime icon
- Duration: 10 seconds

**Start Alarm Modal (center screen, persistent):**
- Size: 400px max-width, centered
- Background: White with tag color border (4px)
- Pulsing animation on border
- Header: Tag color background at 20% opacity
- Blocks interaction with calendar until dismissed
- **Shows task name prominently if associated with schedule**

### Connection Status Indicator

- **Connected**: Green dot in header, "Live"
- **Reconnecting**: Yellow dot, "Connecting..."
- **Disconnected**: Red dot, "Offline - alarms paused"

---

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/alarms/stream` | SSE endpoint for alarm events |
| POST | `/api/alarms/{id}/acknowledge` | Mark alarm as acknowledged |
| GET | `/api/alarms/pending` | Get all pending alarms (for reconnection sync) |

---

## Edge Cases & Constraints

### Timezone Handling
- All times stored and compared in UTC
- Frontend converts to local time for display
- Daylight saving time transitions: alarms may trigger 1 hour early/late during transition hour

### Multiple Schedules Same Time
- If multiple tasks scheduled at same time, emit separate alarms sequentially
- Each alarm has unique ID, user acknowledges each individually

### Navigation on Alarm
- When start alarm fires on any page, app auto-navigates to `/weekly-calendar`
- Calendar component receives signal to highlight the specific day/hour cell
- URL could include query params: `/weekly-calendar?highlightScheduleId=123`
- This ensures user sees exactly which task needs attention

### SSE Connection Loss
- < 60 seconds: Reconnect and receive any missed alarms
- > 60 seconds: On reconnect, fetch `/api/alarms/pending` to sync

### Server Restart
- Pending alarms persisted to database
- Scheduler re-hydrates state on startup
- No lost alarms across restarts

---

## Success Metrics

- **Functional**: Alarms trigger within 5 seconds of target time
- **Reliability**: 99.9% of alarms delivered (tracked via acknowledge receipts)
- **User Engagement**: Users acknowledge > 80% of start alarms

---

## Implementation Notes

### Phase 1: Backend Foundation
1. Create `PendingAlarm` entity and repository
2. Implement `AlarmSchedulerService` with 30s polling
3. Create `AlarmEmitterService` for SSE management
4. Add REST endpoints for acknowledge

### Phase 2: Frontend Core
1. Create `AlarmService` with SSE client
2. Build `AlarmModalComponent` UI
3. Add connection status indicator

### Phase 3: Integration
1. Integrate alarm modal into `WeeklyCalendarComponent`
2. Add auto-reconnect with sync logic
3. Add visual polish (animations, responsive)
4. Implement auto-navigation to weekly calendar on alarm
5. Add cell highlighting in calendar when alarm fires

### Phase 4: Testing
1. Unit tests: scheduler logic, alarm state transitions
2. Integration tests: SSE connection, API endpoints
3. E2E tests: alarm flow from schedule to dismissal

---

## Acceptance Criteria

- [ ] Pre-reminder toast appears 5 minutes before task (fixed)
- [ ] Start alarm modal appears exactly at task start time
- [ ] Alarm auto-navigates to weekly calendar and highlights the scheduled task cell
- [ ] Task name displayed prominently in alarm modal
- [ ] SSE reconnects automatically with exponential backoff (browser handles first 3 attempts)
- [ ] Alarms survive browser refresh (state persisted server-side via lastEventId)
- [ ] Connection status visible in header
- [ ] No "Broken pipe" errors in backend logs (graceful client disconnect handling)
- [ ] Works on Chrome, Firefox, Safari, Edge
- [ ] No missed alarms in 24-hour continuous test

