# Toast Notification Migration - All Features

## Overview

Migrate all remaining features from inline alert banners to the reusable toast notification system. This is a follow-up to the initial toast system implementation in the Tasks feature.

**Reference:** See `@/spec/features/toast-notification.prd.md` for the base toast system specification.

---

## Scope

### Features to Migrate (7 total)

1. **Achievements** - `@/frontend/src/app/features/achievements/`
2. **Calendar** - `@/frontend/src/app/features/calendar/`
3. **Habits** - `@/frontend/src/app/features/habits/`
4. **Metrics** - `@/frontend/src/app/features/metrics/`
5. **Planned Habits** - `@/frontend/src/app/features/planned-habits/`
6. **Tags** - `@/frontend/src/app/features/tags/`
7. **Week Plan** - `@/frontend/src/app/features/week-plan/`

---

## Migration Pattern

Based on the Tasks feature implementation, each feature will follow this pattern:

### 1. Component TypeScript Changes

**Import ToastService:**
```typescript
import { ToastService } from '../../shared/services/toast.service';
```

**Inject Service:**
```typescript
private toastService = inject(ToastService);
```

**Remove Properties:**
- Remove `successMessage: string = ''`
- Remove `errorMessage: string = ''`

**Error Handling Pattern (Universal):**
```typescript
const message = error.error?.message || error.error?.error || error.message || 'Fallback message';
this.toastService.error(message, `Error ${error.status || ''}`);
```

**Success Toast Pattern:**
```typescript
this.toastService.success('Operation completed successfully!');
```

### 2. Component Template Changes

**Remove Inline Alert HTML:**
```html
<!-- REMOVE THESE -->
<div *ngIf="successMessage" class="alert alert-success">{{ successMessage }}</div>
<div *ngIf="errorMessage" class="alert alert-danger">{{ errorMessage }}</div>
```

---

## Feature-Specific Migration Details

### Phase 1: CRUD Features (High Priority)

#### 1. Achievements Component

**Current State:**
- `successMessage` and `errorMessage` properties
- Inline alerts in template
- Used in: `createAchievement`, `deleteAchievement`, `loadAchievements`

**Changes Required:**
- **TypeScript:**
  - Add ToastService import and inject
  - Remove `successMessage` and `errorMessage` properties
  - Update `loadAchievements()` error handler
  - Update `onSubmit()` success/error handlers
  - Update `deleteAchievement()` success/error handlers
  
- **Template:**
  - Remove lines with `*ngIf="successMessage"` and `*ngIf="errorMessage"` alerts

**Success Messages:**
- `'Achievement added successfully!'`
- `'Achievement deleted successfully!'`

---

#### 2. Habits Component

**Current State:**
- `successMessage` and `errorMessage` properties
- Inline alerts in template
- Used in: `createHabit`, `updateHabit`, `deleteHabit`, `loadHabits`

**Changes Required:**
- **TypeScript:**
  - Add ToastService import and inject
  - Remove `successMessage` and `errorMessage` properties
  - Update `loadHabits()` error handler
  - Update `onSubmit()` success/error handlers (both create and update paths)
  - Update `deleteHabit()` success/error handlers
  
- **Template:**
  - Remove inline alert banners

**Success Messages:**
- `'Habit created successfully!'`
- `'Habit updated successfully!'`
- `'Habit deleted successfully!'`

---

#### 3. Tags Component

**Current State:**
- `successMessage` and `errorMessage` properties
- Inline alerts in template
- Used in: `createTag`, `updateTag`, `deleteTag`, `loadTags`, `searchTags`

**Changes Required:**
- **TypeScript:**
  - Add ToastService import and inject
  - Remove `successMessage` and `errorMessage` properties
  - Update `loadTags()` error handler
  - Update `onSubmit()` success/error handlers
  - Update `deleteTag()` success/error handlers
  - Update `searchTags()` error handler
  
- **Template:**
  - Remove inline alert banners

**Success Messages:**
- `'Tag created successfully!'`
- `'Tag updated successfully!'`
- `'Tag deleted successfully!'`

---

### Phase 2: Complex Features

#### 4. Calendar Component

**Current State:**
- `successMessage` and `errorMessage` properties
- More complex interaction patterns
- Used in: `loadHabit`, `loadEntries`, `toggleDayCompletion`, `toggleTodayComplete`, `saveNote`

**Changes Required:**
- **TypeScript:**
  - Add ToastService import and inject
  - Remove `successMessage` and `errorMessage` properties
  - Update `loadHabit()` error handler
  - Update `loadEntries()` error handler
  - Update `toggleDayCompletion()` error handler
  - Update `toggleTodayComplete()` success/error handlers (dynamic message based on state)
  - Update `saveNote()` success/error handlers
  - Remove `setTimeout(() => this.successMessage = '', 3000)` calls
  
- **Template:**
  - Remove inline alert banners

**Success Messages:**
- `'Today marked as complete!'` / `'Today marked as not complete!'` (dynamic)
- `'Note saved successfully!'`

**Note:** Calendar has custom success message clearing with setTimeout - remove this when migrating to toasts.

---

#### 5. Planned Habits Component

**Current State:**
- `successMessage` and `errorMessage` properties
- Batch completion logic with partial failure handling
- Used in: `loadHabits`, `markSelectedAsCompleted`

**Changes Required:**
- **TypeScript:**
  - Add ToastService import and inject
  - Remove `successMessage` and `errorMessage` properties
  - Update `loadHabits()` error handler
  - Update `markSelectedAsCompleted()` - handles both full success and partial failure
    - Success: `'${count} habit(s) marked as completed for today!'`
    - Partial failure: `'${count} habit(s) could not be marked as completed'` (use warning toast)
  - Remove `this.errorMessage = 'Please select at least one habit...'` - use toast instead
  
- **Template:**
  - Remove inline alert banners

**Toast Types:**
- Success: Batch completion succeeded
- Warning: Batch completion had partial failures
- Error: Complete failure or API errors

---

### Phase 3: Read-Only Features (Error Handling Only)

#### 6. Metrics Component

**Current State:**
- `errorMessage` property only (no success messages)
- Multiple data loading operations
- Used in: `loadWeeklyMetrics`, `loadMonthlyMetrics`, `loadTaskMetrics`

**Changes Required:**
- **TypeScript:**
  - Add ToastService import and inject
  - Remove `errorMessage` property
  - Remove `this.errorMessage = ''` clearing calls
  - Update all error handlers to use toast
  
- **Template:**
  - Remove inline error alert

**Note:** This component only shows errors, no success messages (it's a dashboard view).

---

#### 7. Week Plan Component

**Current State:**
- `errorMessage` property only (no success messages)
- Used in: `loadTasksForWeek`

**Changes Required:**
- **TypeScript:**
  - Add ToastService import and inject
  - Remove `errorMessage` property
  - Remove `this.errorMessage = ''` clearing in `loadTasksForWeek()`
  - Update error handler
  
- **Template:**
  - Remove inline error alert

**Note:** This component only shows errors on load failure.

---

## Implementation Timeline

### Phase 1: CRUD Features (Week 1)
- [ ] Day 1: Achievements component
- [ ] Day 2: Habits component
- [ ] Day 3: Tags component

### Phase 2: Complex Features (Week 1-2)
- [ ] Day 4: Calendar component
- [ ] Day 5: Planned Habits component

### Phase 3: Read-Only Features (Week 2)
- [ ] Day 6: Metrics component
- [ ] Day 7: Week Plan component

### Phase 4: Verification (Week 2)
- [ ] Run `npm run build` to verify no compilation errors
- [ ] Test toast notifications in each feature
- [ ] Verify no inline alerts remain in any feature templates

---

## Testing Requirements

### Unit Tests (Optional but Recommended)
- Each migrated component should have its error handling tested
- Verify ToastService methods are called with correct parameters

### Manual Testing Checklist

**For each feature:**
- [ ] Trigger a success action (create/update) → verify success toast appears
- [ ] Trigger an error action (simulate API failure) → verify error toast appears with proper backend message extraction
- [ ] Verify inline alerts no longer appear in the UI
- [ ] Verify toast has correct styling based on type

**Features with special cases:**
- [ ] Calendar: Test both "mark complete" and "mark not complete" messages
- [ ] Planned Habits: Test partial failure scenario (warning toast)

---

## Acceptance Criteria

- [ ] All 7 features migrated to toast notifications
- [ ] No inline alert banners remain in any feature template
- [ ] All `successMessage` and `errorMessage` properties removed from components
- [ ] Consistent error handling pattern across all features
- [ ] Build succeeds without errors
- [ ] Backend error messages display correctly using `error.error?.message || error.error?.error` pattern
- [ ] Success toasts appear for all CRUD operations

---

## Reference Files

**Already Migrated (Reference Implementation):**
- `@/frontend/src/app/features/tasks/tasks.component.ts`
- `@/frontend/src/app/features/tasks/tasks.component.html`

**Toast System Files:**
- `@/frontend/src/app/shared/services/toast.service.ts`
- `@/frontend/src/app/shared/components/toast/toast.component.ts`
- `@/frontend/src/app/shared/models/toast.model.ts`

---

## Notes

- The ToastContainer is already added to AppComponent (from Phase 1 implementation)
- No changes needed to the toast system itself
- Each feature migration is independent - can be done in parallel by different developers
- Maintain existing component logic - only replace alert handling
