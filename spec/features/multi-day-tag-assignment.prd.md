---
version: 1.0.0
last_updated: 2026-04-05
author: AI
---

# Multi-Day Tag Assignment

## 1. Overview / Objective

Enable users to assign a tag to multiple days of the week simultaneously when clicking on a calendar cell. Currently, users can only assign a tag to a single day/hour combination. This feature allows users to efficiently schedule recurring tags across selected days (e.g., Monday, Wednesday, Friday) or the entire week.

## 2. Target Users

- Users who want to schedule the same tag across multiple days at the same time slot
- Users with recurring weekly routines (e.g., gym on Mon/Wed/Fri)

## 3. Functional Requirements

### 3.1 Core Behavior
- When a user clicks any calendar cell, the tag selection modal opens with the clicked day pre-selected
- A day selector is displayed showing all 7 days of the week as toggle buttons
- Users can toggle any combination of days (Mon-Sun) plus an "All" option
- At least one day must remain selected
- When a tag is selected, it is assigned to all selected days at the same hour

### 3.2 Day Selector States
| State | Behavior |
|-------|----------|
| Default | Clicked day is pre-selected; others are unselected |
| Toggle On | Clicking an unselected day adds it to the selection |
| Toggle Off | Clicking a selected day removes it (unless it's the only one) |
| All Button | Toggles all days on/off |
| Validation | Cannot deselect the last remaining day |

### 3.3 UX Flow
1. User clicks cell (e.g., Tuesday 14:00)
2. Modal opens with Tuesday pre-selected in day selector
3. User can add/remove days from selection
4. User selects a tag
5. System creates schedule entries for all selected days at 14:00 with the chosen tag
6. Modal closes, calendar refreshes showing all new assignments

## 4. Non-Functional Requirements

- All selected days must be saved atomically (success/failure together)
- UI must remain responsive during bulk save operations
- Day selector must be intuitive and provide visual feedback

## 5. Technical Requirements

### 5.1 Frontend Changes
**Approach**: Frontend-only (Option 1) - Parallel API calls using RxJS `forkJoin`

**Files Affected**:
- `@kotlin/track-daily-routine/frontend/src/app/features/week-calendar/week-calendar.component.ts`
- `@kotlin/track-daily-routine/frontend/src/app/features/week-calendar/week-calendar.component.html`
- `@kotlin/track-daily-routine/frontend/src/app/features/week-calendar/week-calendar.component.css`

**Implementation Details**:
1. Add new signals:
   - `selectedDays: number[]` - array of selected day indices (0-6)
   - Keep existing `selectedCell` for reference to hour and original day

2. Add helper methods:
   - `toggleDay(dayIndex: number)` - toggle selection state
   - `toggleAllDays()` - select/deselect all
   - `isDaySelected(dayIndex: number)` - check selection state

3. Modify `selectTag()` method:
   ```typescript
   // Current: single API call
   // New: Use forkJoin to save for all selected days
   const requests = this.selectedDays().map(dayOfWeek => ({
     dayOfWeek,
     hour: cell.hour,
     weekStartDate,
     tagId: tag.id
   }));
   
   forkJoin(requests.map(r => this.weekCalendarService.saveSchedule(r)))
     .subscribe({
       next: () => {
         this.loadWeekSchedule();
         this.closeTagModal();
       },
       error: (err) => console.error('Failed to save schedules:', err)
     });
   ```

4. Modify `onCellClick()` to initialize `selectedDays` with the clicked day

5. Update modal template to include day selector between subtitle and tag list

### 5.2 API Usage
- Reuses existing `POST /week-calendar` endpoint
- No backend changes required
- Each day generates one `CreateWeekScheduleRequest`

### 5.3 Data Model
No changes to existing models:
- `WeekSchedule` interface unchanged
- `CreateWeekScheduleRequest` unchanged (called once per day)

## 6. UI / UX

### 6.1 Modal Layout Update
```
┌─ Assign Tag ─────────────┐
│ Tuesday at 14:00         │
│                           │
│ [Mon] [Tue✓] [Wed] [Thu] │  <- Toggle buttons, Tue pre-selected
│ [Fri] [Sat] [Sun] [All]  │
│                           │
│ ○ Tag 1                   │
│ ○ Tag 2                   │
│                           │
│ [Remove]    [Cancel]      │
└──────────────────────────┘
```

### 6.2 Day Toggle Button States
- **Unselected**: Gray background, gray text
- **Selected**: Primary color background (blue), white text
- **Disabled** (when trying to deselect last day): Visual indication of locked state
- **Hover**: Slight opacity change or border highlight

### 6.3 Interaction Rules
- Clicking day toggles selection
- "All" button selects all days if any are unselected; deselects all if all are selected
- Minimum one day must remain selected (validation prevents unselecting last day)
- Short day names used: Mon, Tue, Wed, Thu, Fri, Sat, Sun

## 7. Success Metrics

- User can assign a tag to multiple days in a single action
- All selected days show the assigned tag after modal closes
- No performance degradation with bulk saves (max 7 parallel requests)
- Feature is discoverable and intuitive to use

## 8. Edge Cases / Constraints

| Scenario | Handling |
|----------|----------|
| User tries to deselect all days | Prevent action; keep at least one day selected |
| One API call fails | Currently: partial failure accepted. Future: consider rollback or retry |
| Cell already has tag on some days | Overwrites with new selection (existing behavior) |
| User clicks "Remove Assignment" | Removes tag only from originally clicked day (existing behavior) |
| Slow network | Loading state should indicate operation in progress |

## 9. Implementation Notes

### 9.1 Component State Changes
```typescript
// Add to existing signals
selectedDays = signal<number[]>([]);  // Tracks selected days for multi-assignment

// Modify existing methods
onCellClick(cell: CalendarCell): void {
  this.selectedCell.set(cell);
  this.selectedDays.set([cell.dayOfWeek]);  // Initialize with clicked day
  this.showTagModal.set(true);
}

// Add new methods
toggleDay(dayIndex: number): void {
  const current = this.selectedDays();
  if (current.includes(dayIndex)) {
    if (current.length > 1) {
      this.selectedDays.set(current.filter(d => d !== dayIndex));
    }
  } else {
    this.selectedDays.set([...current, dayIndex]);
  }
}

toggleAllDays(): void {
  const current = this.selectedDays();
  const allDays = [0, 1, 2, 3, 4, 5, 6];
  if (current.length === 7) {
    // Keep at least the originally clicked day
    this.selectedDays.set([this.selectedCell()!.dayOfWeek]);
  } else {
    this.selectedDays.set(allDays);
  }
}
```

### 9.2 Template Addition
```html
<!-- Day Selector -->
<div class="day-selector" *ngIf="selectedCell()">
  <div class="day-toggles">
    <button 
      *ngFor="let day of days; let i = index"
      class="day-toggle"
      [class.selected]="isDaySelected(i)"
      (click)="toggleDay(i)">
      {{ day }}
    </button>
    <button class="day-toggle all-btn" (click)="toggleAllDays()">
      All
    </button>
  </div>
</div>
```

### 9.3 CSS Considerations
- Day toggles: flex layout, equal sizing
- Selected state: distinct visual from tag selection
- Responsive: maintain usability on smaller screens

## 10. Future Enhancements (Out of Scope)

- Copy schedule from one week to another
- Recurring patterns (every X weeks)
- Backend batch endpoint for atomic saves
- Undo/rollback functionality for partial failures
