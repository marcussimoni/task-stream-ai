---
version: 1.0.0
last_updated: 2026-04-11
author: AI
---

# Database Backup & Restore - Product Requirements Document

## Overview

An admin panel feature that allows users to manually create database backups and restore from existing backup files. The feature provides a simple interface to list available backups, create new backups on-demand, and restore the database from a selected backup file with proper safety confirmations.

---

## Goals

1. Provide manual database backup creation through the UI
2. Allow users to view all available backup files with metadata
3. Enable safe database restoration with clear warnings and confirmations
4. Display operation results via toast notifications
5. Ensure only authorized users can access backup operations

---

## Target Users

- All authenticated users (backup creation and restoration)
- Users accessing the feature through the Admin Panel section

---

## Functional Requirements

### 1. Backup Operations

#### 1.1 Create Backup
- **Endpoint**: `GET /api/backup-database`
- **Response**: `BackupCreatedDTO`
```json
{
  "dbFilename": "db_backup_2025_01_15_143022.db",
  "sqlFilename": "sql_backup_2025_01_15_143022.sql",
  "timestamp": "2025-01-15T14:30:22",
  "status": "success"
}
```
- **Action**: Button labeled "Create Backup" in the UI
- **Feedback**: Toast notification displaying the response `status` and `dbFilename`

#### 1.2 List Backup Files
- **Endpoint**: `GET /api/backup-database/backup-files`
- **Response**: `List<BackupFileDTO>`
```json
[
  {
    "filename": "db_backup_2025_01_15_143022.db",
    "directory": "/backups",
    "size": "1.2 MB",
    "createdAt": "2025-01-15T14:30:22"
  }
]
```
- **Display**: Table showing all fields (filename, directory, size, createdAt)
- **Sorting**: Newest backups first (by `createdAt` descending)

#### 1.3 Restore Database
- **Endpoint**: `GET /api/backup-database/restore?filename={filename}`
- **Response**: `BackupRestoredDTO`
```json
{
  "filename": "db_backup_2025_01_15_143022.db",
  "timestamp": "2025-01-15T14:35:10",
  "status": "success"
}
```
- **Confirmation**: Modal dialog with explicit warning
- **Warning Message**: "This process will delete all existing data and replace it with the selected backup file. This action cannot be undone."
- **Confirmation Button**: "Restore Backup" (danger/red styling)
- **Feedback**: Toast notification displaying the response `status` and `filename`

### 2. Admin Panel Section

#### 2.1 Navigation
- Location: Admin Panel section
- Menu Item: "Database Backup"
- Icon: Database or cloud backup icon (Lucide: `Database` or `Archive`)

#### 2.2 Layout
- **Header**: "Database Backup & Restore"
- **Actions Bar**: "Create Backup" button (primary)
- **Table Section**: List of available backups with restore actions

### 3. Toast Notifications

All operations display results using the existing toast notification system:
- **Success**: Green toast with backend response message
- **Error**: Red toast with backend error message
- **Info**: Blue toast for user guidance (e.g., "Select a backup to restore")

---

## Non-Functional Requirements

### 1. Performance
- Backup list loads within 2 seconds
- Create/restore operations show loading state during API call
- No UI blocking during operations

### 2. Security
- All endpoints require authentication (existing JWT)
- Restore operation requires explicit user confirmation
- No delete functionality exposed (backend-only cleanup)

### 3. Usability
- Clear date/time formatting (locale-aware)
- Loading indicators on buttons during operations
- Disabled state for buttons while operation in progress
- Empty state message when no backups exist

---

## Technical Requirements

### Architecture
```
frontend/src/app/
├── features/
│   └── admin/
│       ├── components/
│       │   └── database-backup/
│       │       ├── database-backup.component.ts
│       │       ├── database-backup.component.html
│       │       └── database-backup.component.scss
│       ├── models/
│       │   └── backup.model.ts
│       └── services/
│           └── backup.service.ts
```

### Models

#### BackupFileDTO
```typescript
export interface BackupFileDTO {
  filename: string;
  directory: string;
  size: string;
  createdAt: string; // ISO 8601 format
}
```

#### BackupCreatedDTO
```typescript
export interface BackupCreatedDTO {
  dbFilename: string;
  sqlFilename: string;
  timestamp: string;
  status: string;
}
```

#### BackupRestoredDTO
```typescript
export interface BackupRestoredDTO {
  filename: string;
  timestamp: string;
  status: string;
}
```

### Service Design

#### BackupService
```typescript
export class BackupService {
  private http = inject(HttpClient);
  private apiUrl = '/api/backup-database';

  getBackupFiles(): Observable<BackupFileDTO[]>
  createBackup(): Observable<BackupCreatedDTO>
  restoreBackup(filename: string): Observable<BackupRestoredDTO>
}
```

### Component Design

#### DatabaseBackupComponent
- Standalone Angular 21 component
- Uses Signals for state management:
  - `backups`: Signal<BackupFileDTO[]>
  - `loading`: Signal<boolean>
  - `operationInProgress`: Signal<boolean>
- Methods:
  - `loadBackups()`: Fetch and populate backup list
  - `createBackup()`: Trigger backup creation
  - `restoreBackup(filename: string)`: Open confirmation modal
  - `confirmRestore(filename: string)`: Execute restore after confirmation

### UI Components
- **Table**: Angular Material Table or custom HTML table
- **Buttons**: 
  - "Create Backup" - primary style
  - "Restore" - outlined/warn style per row
- **Modal**: Confirmation dialog for restore operation
- **Icons**: Lucide Angular icons (`Database`, `Download`, `Upload`, `AlertTriangle`)

---

## UI / UX Specifications

### Page Layout
```
┌─────────────────────────────────────────────────────────┐
│  Database Backup & Restore                              │
├─────────────────────────────────────────────────────────┤
│  [Create Backup]                                        │
├─────────────────────────────────────────────────────────┤
│  ┌─────────────────────────────────────────────────┐    │
│  │ Available Backups                               │    │
│  ├──────────┬──────────┬─────────────┬─────────────┤    │
│  │ Filename │ Size     │ Created At  │ Actions     │    │
│  ├──────────┼──────────┼─────────────┼─────────────┤    │
│  │ backup1  │ 1.2 MB   │ Jan 15, 2025│ [Restore]   │    │
│  │ backup2  │ 1.1 MB   │ Jan 14, 2025│ [Restore]   │    │
│  └──────────┴──────────┴─────────────┴─────────────┘    │
│                                                         │
│  No backups available (empty state)                     │
└─────────────────────────────────────────────────────────┘
```

### Restore Confirmation Modal
```
┌─────────────────────────────────────────┐
│  ⚠️ Restore Database                     │
├─────────────────────────────────────────┤
│                                         │
│  Warning:                               │
│  This process will delete all existing  │
│  data and replace it with the selected  │
│  backup file.                           │
│                                         │
│  File: db_backup_2025_01_15.db          │
│                                         │
│  This action cannot be undone.          │
│                                         │
│  [Cancel]          [Restore Backup]     │
└─────────────────────────────────────────┘
```

### Styling
- Use existing SCSS variables for colors, spacing, typography
- Responsive design: table scrolls horizontally on mobile
- Danger/warning colors for destructive actions
- Consistent with existing admin panel styling

---

## Success Metrics

- Users can successfully create backups with one click
- Backup list displays all metadata correctly
- Restore operation requires confirmation and succeeds
- Toast notifications appear for all operations
- Page loads within 2 seconds
- No data loss incidents due to accidental restores

---

## Edge Cases / Constraints

### Edge Cases
1. **No Backups Exist**: Display empty state with message "No backups available. Click 'Create Backup' to create your first backup."
2. **Restore in Progress**: Disable all action buttons while restore operation is running
3. **API Failure**: Show error toast with backend message; keep UI consistent
4. **Long Backup List**: Implement pagination or virtual scrolling if list exceeds 50 items
5. **Concurrent Operations**: Prevent multiple simultaneous backup/restore operations

### Constraints
- No delete functionality in UI (per requirements)
- Manual operations only (no scheduling UI)
- All users can perform operations (no role-based restrictions)
- Toast notifications only (no inline alerts)

---

## Implementation Notes

### Files Affected
- **New Files**:
  - `frontend/src/app/features/admin/components/database-backup/database-backup.component.ts`
  - `frontend/src/app/features/admin/components/database-backup/database-backup.component.html`
  - `frontend/src/app/features/admin/components/database-backup/database-backup.component.scss`
  - `frontend/src/app/features/admin/models/backup.model.ts`
  - `frontend/src/app/features/admin/services/backup.service.ts`

- **Modified Files**:
  - Admin panel routing module (add new route)
  - Admin panel navigation (add menu item)

### Backend Dependencies
- `@/src/main/kotlin/br/com/dailytrack/controller/BackupDatabaseController.kt`
- Endpoints:
  - `GET /api/backup-database`
  - `GET /api/backup-database/restore?filename={filename}`
  - `GET /api/backup-database/backup-files`

### Integration Points
- Toast notification service (existing)
- Authentication/authorization (existing JWT)
- HTTP client (existing Angular HttpClient)

### Testing Requirements

#### Unit Tests
- `BackupService`: All HTTP methods, error handling
- `DatabaseBackupComponent`: 
  - State initialization
  - Load backups on init
  - Create backup flow
  - Restore confirmation modal
  - Error handling

#### E2E Tests
- Navigate to Database Backup page
- Create backup and verify toast
- Verify backup appears in list
- Restore backup with confirmation
- Verify success toast after restore
- Cancel restore confirmation (no operation)

---

## Acceptance Criteria

- [ ] Admin Panel has "Database Backup" menu item
- [ ] Page displays "Create Backup" button
- [ ] Backup list table shows all BackupFileDTO fields
- [ ] Create backup triggers API call and shows success toast
- [ ] Each backup row has "Restore" button
- [ ] Restore button opens confirmation modal with warning message
- [ ] Cancel in modal closes without action
- [ ] Confirm in modal triggers restore API and shows toast
- [ ] Error responses display in toast notifications
- [ ] Loading states prevent duplicate operations
- [ ] Empty state displays when no backups exist
- [ ] Page is responsive on mobile devices
- [ ] All existing tests pass
