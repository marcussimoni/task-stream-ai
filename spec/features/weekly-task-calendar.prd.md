# Product Requirement Document (PRD) - Weekly Task Calendar Feature

## 1. Feature Overview
**Feature Name:** Weekly Task Calendar  
**Description:** A week-based calendar view (similar to Microsoft Teams calendar) that allows users to plan their week by assigning tags to specific hours. Each assigned tag automatically loads an incomplete task for that tag, giving users a clear overview of what needs to be done and when.

**Objective:**
- Provide a visual weekly schedule showing planned tasks by hour
- Allow users to assign tags to specific day/hour slots
- Automatically load incomplete tasks for the configured tag at each slot
- Track task progress visually within the calendar context
- Solve the problem of knowing which tasks are planned for the week, their progress, and when to start working on them

## 2. Target Users
- All users with access to the system who want to plan their weekly task schedule
- Users who work with multiple tags/categories and need time-based planning
- Users who want a visual overview of task distribution throughout the week

## 3. Functional Requirements

### 3.1 Calendar Display
- Display a weekly calendar view showing 7 days (Monday to Sunday)
- Time range: 8:00 AM to 10:00 PM (14 hours)
- Grid layout: Days as columns, hours as rows
- Each cell represents a specific day/hour combination (e.g., Monday 9:00 AM)
- Visual indicators for current day and current time
- Navigation to previous/next week
- Quick "Today" button to return to current week

### 3.2 Tag Assignment
- Click on any empty day/hour cell to assign a tag
- Open a tag selection modal showing all available tags (from Tags feature)
- Each tag displays its name and color for easy identification
- Only one tag can be assigned per day/hour cell
- Click on an assigned tag to change it or remove it
- Visual representation of assigned tag with its color in the calendar cell

### 3.3 Task Loading
- When a tag is assigned to a day/hour cell:
  - Automatically load ONE incomplete (non-completed) task associated with that tag
  - If multiple incomplete tasks exist for the tag, select the one with:
    - Earliest end date (most urgent), OR
    - Highest current progress (closest to completion)
  - Display task name, current progress percentage, and completion status
  - If no incomplete tasks exist for the tag, show "No pending tasks" message

### 3.4 Task Progress Display
- Show task progress visually within the calendar cell:
  - Progress bar or percentage indicator
  - Color-coded: Green for high progress (>70%), Yellow for medium (30-70%), Red for low (<30%)
  - Show currentValue/targetValue (e.g., "45/100")
- Completed tasks are not shown in the calendar (only incomplete)

### 3.5 Task Interaction
- Click on a task in a calendar cell to view task details
- Option to navigate to the full Tasks page for editing
- Visual indicator for task completion status

### 3.6 Data Persistence
- Save tag assignments to the database (new entity: WeekSchedule)
- Each schedule entry contains: dayOfWeek (0-6), hour (8-22), tagId
- Persist assignments automatically when user makes changes

## 4. User Interface / UX

### 4.1 Layout Structure
- **Header Section:**
  - Week navigation (Previous / Current Week / Next)
  - Week range display (e.g., "April 1 - April 7, 2026")
  - "Today" button
  - Toggle view option (compact/comfortable)

- **Calendar Grid:**
  - **Columns:** 7 days (Mon-Sun) with date display
  - **Rows:** 14 hours (8 AM - 10 PM) in 1-hour blocks
  - **Current Time Indicator:** Horizontal line showing current time
  - **Cell Height:** Minimum 60px for comfortable interaction

- **Cell Content (when tag assigned):**
  - Tag badge with color indicator
  - Task name (truncated if too long)
  - Progress bar
  - Progress text (e.g., "45%")

### 4.2 Empty State
- Empty cells show light gray background with "+" icon on hover
- Tooltip: "Click to assign tag"

### 4.3 Tag Selection Modal
- Modal title: "Assign Tag"
- List of all tags with:
  - Color circle indicator
  - Tag name
  - Usage count (optional)
- Search/filter capability for tags
- "Remove Assignment" button (when editing existing assignment)
- Cancel and Save buttons

### 4.4 Task Detail Overlay
- On task click, show tooltip/popover with:
  - Full task name
  - Task description (if available)
  - Complete progress details: "45/100 (45%)"
  - Start date and end date
  - "Edit Task" link to navigate to Tasks page
  - "Mark Complete" quick action (optional)

### 4.5 Responsive Behavior
- Desktop: Full 7-day grid visible
- Tablet: Horizontal scroll or day selector
- Mobile: Day-by-day view with horizontal navigation

## 5. Technical Requirements

### 5.1 Frontend Implementation

**New Components:**
- `WeekCalendarComponent` at `frontend/src/app/features/week-calendar/`
  - `week-calendar.component.ts`
  - `week-calendar.component.html`
  - `week-calendar.component.css`

**New Models:**
- `WeekSchedule` at `frontend/src/app/core/models/week-schedule.model.ts`
```typescript
export interface WeekSchedule {
  id?: number;
  dayOfWeek: number; // 0 = Monday, 6 = Sunday
  hour: number; // 8-22
  tagId: number;
  tag?: Tag;
  task?: Task; // Computed/loaded task
  weekStartDate?: string; // ISO date string for week identification
  createdAt?: string;
  updatedAt?: string;
}
```

**New Service:**
- `WeekCalendarService` at `frontend/src/app/core/services/week-calendar.service.ts`
```typescript
getWeekSchedule(weekStartDate: string): Observable<WeekSchedule[]>
saveSchedule(schedule: WeekSchedule): Observable<WeekSchedule>
deleteSchedule(scheduleId: number): Observable<void>
getTaskForTag(tagId: number): Observable<Task | null>
```

**Routing:**
- Add route `/week-calendar` to app routing
- Add navigation menu item in main navigation

**Dependencies:**
- Reuse existing Tag and Task services
- No new external libraries required (use existing Angular/CDK)

### 5.2 Backend Implementation

**New Entity - WeekSchedule:**
```kotlin
@Entity
@Table(name = "week_schedules")
data class WeekSchedule(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val dayOfWeek: Int, // 0-6 (Monday-Sunday)

    @Column(nullable = false)
    val hour: Int, // 8-22

    @Column(nullable = false)
    val weekStartDate: LocalDate, // First day of the week (Monday)

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "tag_id", nullable = false)
    var tag: Tag,
)
```

**New Repository:**
- `WeekScheduleRepository` at `br/com/dailytrack/repository/WeekScheduleRepository.kt`
```kotlin
@Repository
interface WeekScheduleRepository : JpaRepository<WeekSchedule, Long> {
    fun findByWeekStartDate(weekStartDate: LocalDate): List<WeekSchedule>
    fun findByWeekStartDateAndDayOfWeekAndHour(weekStartDate: LocalDate, dayOfWeek: Int, hour: Int): WeekSchedule?
    fun deleteByWeekStartDateAndDayOfWeekAndHour(weekStartDate: LocalDate, dayOfWeek: Int, hour: Int)
}
```

**New DTOs:**
- `WeekScheduleDTO` at `br/com/dailytrack/dto/WeekScheduleDTO.kt`
```kotlin
data class WeekScheduleDTO(
    val id: Long?,
    val dayOfWeek: Int,
    val hour: Int,
    val weekStartDate: String, // ISO format
    val tagId: Long,
    val tag: TagDTO?,
    val task: TaskDTO?
)
```

- `CreateWeekScheduleRequest` at `br/com/dailytrack/dto/CreateWeekScheduleRequest.kt`
```kotlin
data class CreateWeekScheduleRequest(
    val dayOfWeek: Int,
    val hour: Int,
    val weekStartDate: String,
    val tagId: Long
)
```

**New Service:**
- `WeekScheduleService` at `br/com/dailytrack/service/WeekScheduleService.kt`
```kotlin
@Service
class WeekScheduleService(
    private val scheduleRepository: WeekScheduleRepository,
    private val taskRepository: TaskRepository,
    private val tagRepository: TagRepository
) {
    fun getWeekSchedule(weekStartDate: LocalDate): List<WeekScheduleDTO>
    fun createOrUpdateSchedule(request: CreateWeekScheduleRequest): WeekScheduleDTO
    fun deleteSchedule(weekStartDate: LocalDate, dayOfWeek: Int, hour: Int)
    fun findTaskForTag(tagId: Long): Task? // Finds incomplete task for tag
}
```

**New Controller:**
- `WeekCalendarController` at `br/com/dailytrack/controller/WeekCalendarController.kt`
```kotlin
@RestController
@RequestMapping("/api/week-calendar")
class WeekCalendarController(private val weekScheduleService: WeekScheduleService) {

    @GetMapping
    fun getWeekSchedule(@RequestParam weekStartDate: String): ResponseEntity<List<WeekScheduleDTO>>

    @PostMapping
    fun createSchedule(@RequestBody request: CreateWeekScheduleRequest): ResponseEntity<WeekScheduleDTO>

    @DeleteMapping("/{id}")
    fun deleteSchedule(@PathVariable id: Long): ResponseEntity<Void>

    @GetMapping("/task-for-tag")
    fun getTaskForTag(@RequestParam tagId: Long): ResponseEntity<TaskDTO?>
}
```

**New Mapper:**
- `WeekScheduleMapper` at `br/com/dailytrack/mapper/WeekScheduleMapper.kt`

### 5.3 API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/week-calendar?weekStartDate=2026-04-01` | Get all schedule entries for a week |
| POST | `/api/week-calendar` | Create or update a schedule entry |
| DELETE | `/api/week-calendar/{id}` | Delete a schedule entry |
| GET | `/api/week-calendar/task-for-tag?tagId=1` | Get an incomplete task for a tag |

### 5.4 Database Migration
```sql
CREATE TABLE week_schedules (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    day_of_week INT NOT NULL CHECK (day_of_week BETWEEN 0 AND 6),
    hour INT NOT NULL CHECK (hour BETWEEN 8 AND 22),
    week_start_date DATE NOT NULL,
    tag_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    FOREIGN KEY (tag_id) REFERENCES tags(id),
    UNIQUE KEY unique_schedule_slot (week_start_date, day_of_week, hour)
);
```

## 6. Metrics and Success Criteria

### 6.1 Success Criteria
- Calendar displays 7 days × 14 hours grid correctly
- User can assign tags to any day/hour cell
- Tag assignments persist after page refresh
- Incomplete tasks load automatically when tag is assigned
- Task progress is visually displayed in calendar cells
- User can navigate between weeks
- Mobile responsive design works correctly
- Build completes without errors

### 6.2 User Experience Metrics
- Tag assignment should take < 3 clicks
- Calendar load time < 2 seconds
- Task loading after tag assignment < 1 second

## 7. Edge Cases and Constraints

### 7.1 Edge Cases
- **No Tags Created:** Show message "Create tags first to use the calendar" with link to Tags page
- **No Incomplete Tasks for Tag:** Display "No pending tasks" in the cell
- **All Tasks Completed:** When a tag has no incomplete tasks, show completion message
- **Overlapping/Conflict:** Each day/hour can only have one tag (enforced by unique constraint)
- **Week Boundary:** Handle week transitions correctly across month/year boundaries
- **Timezone:** Store and display all times in local timezone
- **Database Constraint Violation:** Handle unique constraint violation gracefully with user-friendly message

### 7.2 Constraints
- Maximum one tag per day/hour cell
- Hours limited to 8 AM - 10 PM range
- Only incomplete tasks are displayed
- Task selection algorithm prioritizes urgency (end date) then progress

## 8. Implementation Notes

### 8.1 Files to Create

**Frontend:**
- `frontend/src/app/features/week-calendar/week-calendar.component.ts`
- `frontend/src/app/features/week-calendar/week-calendar.component.html`
- `frontend/src/app/features/week-calendar/week-calendar.component.css`
- `frontend/src/app/core/models/week-schedule.model.ts`
- `frontend/src/app/core/services/week-calendar.service.ts`

**Backend:**
- `src/main/kotlin/br/com/dailytrack/model/WeekSchedule.kt`
- `src/main/kotlin/br/com/dailytrack/dto/WeekScheduleDTO.kt`
- `src/main/kotlin/br/com/dailytrack/dto/CreateWeekScheduleRequest.kt`
- `src/main/kotlin/br/com/dailytrack/repository/WeekScheduleRepository.kt`
- `src/main/kotlin/br/com/dailytrack/service/WeekScheduleService.kt`
- `src/main/kotlin/br/com/dailytrack/controller/WeekCalendarController.kt`
- `src/main/kotlin/br/com/dailytrack/mapper/WeekScheduleMapper.kt`

### 8.2 Files to Modify

**Frontend:**
- `frontend/src/app/app-routing.module.ts` - Add week-calendar route
- `frontend/src/app/app.component.html` - Add navigation menu item
- `frontend/src/app/core/models/index.ts` - Export WeekSchedule model

**Backend:**
- `src/main/resources/db/migration/` - Create Flyway migration for new table

### 8.3 Task Selection Algorithm
```kotlin
fun findTaskForTag(tagId: Long): Task? {
    val incompleteTasks = taskRepository.findByTagIdAndCompletedFalse(tagId)
    
    return incompleteTasks.minWithOrNull(compareBy(
        { it.endDate ?: LocalDate.MAX }, // Prioritize earliest end date
        { -(it.currentValue.toDouble() / it.targetValue) } // Then highest progress
    ))
}
```

## 9. Future Enhancements (Out of Scope)
- Drag and drop to move tag assignments
- Recurring weekly schedules
- Color-code cells based on task urgency
- Integration with calendar exports (iCal/Google Calendar)
- Notifications/reminders for scheduled tasks
- Multiple tasks per time slot
- Custom time ranges (user-configurable)
