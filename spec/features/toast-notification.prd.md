# Toast Notification System - Product Requirements Document

## Overview

A reusable, standalone Angular toast notification system to replace inline alert banners in the application. The system will display non-intrusive notifications as stacked toast messages at the top-right of the screen with auto-dismiss capability and manual close functionality.

---

## Goals

1. Replace inline success/error alerts with modern toast notifications
2. Provide a reusable component usable across all features
3. Support multiple notification types (success, error, warning, info)
4. Handle backend error messages properly
5. Ensure smooth user experience with animations and stacking

---

## Functional Requirements

### 1. Toast Position & Layout
- **Position**: Fixed at top-right corner of viewport
- **Stacking**: Vertical stack, newest toast appears at top
- **Z-index**: 9999 (above all other content)
- **Spacing**: 16px margin from edges, 8px between stacked toasts

### 2. Display Behavior
- **Auto-dismiss**: Toast automatically disappears after 5 seconds
- **Pause on hover**: Auto-dismiss timer pauses when user hovers over toast
- **Resume on leave**: Timer resumes when mouse leaves toast
- **Manual dismiss**: Each toast has a close (×) button
- **Max visible toasts**: 5 (oldest auto-dismisses if exceeded)

### 3. Notification Types
| Type | Icon | Background Color | Text Color | Use Case |
|------|------|------------------|------------|----------|
| Success | Checkmark | #d4edda (light green) | #155724 (dark green) | Task created/updated |
| Error | X | #f8d7da (light red) | #721c24 (dark red) | Server errors |
| Warning | Triangle | #fff3cd (light yellow) | #856404 (dark yellow) | Validation warnings |
| Info | Info circle | #d1ecf1 (light blue) | #0c5460 (dark blue) | General information |

### 4. Backend Error Handling
Handle backend error response format:
```json
{
  "timestamp": "2026-04-04T12:54:52.085315",
  "status": 500,
  "error": "Internal Server Error",
  "message": "An unexpected error occurred",
  "path": "/api/metrics/weekly"
}
```

- Display `message` field as toast content
- Fallback to `error` if `message` is empty
- Include `status` code in toast title for debugging

### 5. Animations
- **Enter**: Slide in from right (translateX: 100% → 0%) with fade in
- **Exit**: Slide out to right (translateX: 0% → 100%) with fade out
- **Duration**: 300ms ease-in-out

---

## Technical Requirements

### Architecture
```
frontend/src/app/shared/
├── components/
│   └── toast/
│       ├── toast.component.ts
│       ├── toast.component.html
│       └── toast.component.scss
├── services/
│   └── toast.service.ts
├── models/
│   └── toast.model.ts
└── interceptors/
    └── error-toast.interceptor.ts (optional)
```

### Component Design

#### Toast Model
```typescript
export interface Toast {
  id: string;
  type: 'success' | 'error' | 'warning' | 'info';
  title?: string;
  message: string;
  duration?: number; // milliseconds, default 5000
  dismissible?: boolean; // default true
}
```

#### Toast Service
```typescript
export class ToastService {
  private toasts = signal<Toast[]>([]);
  readonly activeToasts = this.toasts.asReadonly();

  success(message: string, title?: string): void
  error(message: string, title?: string): void
  warning(message: string, title?: string): void
  info(message: string, title?: string): void
  show(toast: Partial<Toast>): void
  dismiss(toastId: string): void
  clear(): void
}
```

#### Toast Component
- Standalone Angular 21 component
- Uses `position: fixed` for viewport positioning
- Signals for state management
- Auto-dismiss with `setTimeout` (cleared on hover)

### Styling (SCSS)
- Use CSS Grid/Flexbox for stacking
- CSS transitions for animations
- Responsive: 320px width on desktop, 100% width on mobile (< 640px)
- Box shadow: 0 4px 12px rgba(0, 0, 0, 0.15)

---

## Migration Plan

### Phase 1: Create Toast System
1. Create Toast model interface
2. Implement ToastService with signal-based state
3. Build ToastContainer component with animations
4. Add global styles and positioning

### Phase 2: Update Tasks Component
1. Remove inline alerts from `tasks.component.html` (lines 5-11)
2. Inject ToastService into TasksComponent
3. Replace `successMessage` and `errorMessage` logic with toast calls
4. Handle backend error format extraction

### Phase 3: Integration
1. Add ToastContainer to AppComponent (single global instance)
2. Optional: Create HTTP interceptor to auto-show error toasts

---

## Usage Examples

### Basic Usage in Component
```typescript
export class TasksComponent {
  private toastService = inject(ToastService);

  createTask(task: Task): void {
    this.taskService.create(task).subscribe({
      next: () => {
        this.toastService.success('Task created successfully');
      },
      error: (error) => {
        const message = error.error?.message || error.error?.error || 'Failed to create task';
        this.toastService.error(message, `Error ${error.status}`);
      }
    });
  }
}
```

### Template Setup (AppComponent)
```html
<app-toast-container></app-toast-container>
<router-outlet></router-outlet>
```

---

## Testing Requirements

### Unit Tests
- ToastService: add/remove/clear toasts, auto-dismiss timing
- ToastComponent: render correct content, close button, hover pause
- Integration: service and component communication via signals

### E2E Tests
- Toast appears on CRUD operations
- Stacking behavior with multiple toasts
- Auto-dismiss and manual dismiss work correctly

---

## Acceptance Criteria

- [ ] Toast appears at top-right with slide-in animation
- [ ] Multiple toasts stack vertically (max 5 visible)
- [ ] Each toast has auto-dismiss (5s) and manual close (× button)
- [ ] Hover pauses auto-dismiss timer
- [ ] Backend error messages display correctly from error payload
- [ ] Toasts replace inline alerts in Tasks component
- [ ] Component is reusable across all features
- [ ] Works on desktop and mobile (responsive)
