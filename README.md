# Task Stream AI - Study Tracking & Task Management Platform

A full-stack Kotlin Spring Boot + Angular application for tracking daily study activities, habits, tasks, and achievements with AI-powered features.

## Overview

This is a comprehensive productivity platform that helps users track their study habits, manage tasks, and visualize their progress through interactive dashboards and analytics.

## Requirements

### Prerequisites
- **Java 24+**
- **Maven 3.6+**
- **Node.js 18+** (for frontend)
- **H2 Database** (embedded, no separate installation needed)
- **Ollama** (optional, for AI features)
- **GraalVM 24+** (optional, for native compilation)
- **Electron** (optional, for desktop app)

### Tech Stack

**Backend:**
- **Language**: Kotlin 2.2.21
- **Framework**: Spring Boot 4.1.0-M4
- **Database**: H2 (embedded)
- **Migrations**: Flyway
- **ORM**: Spring Data JPA (Hibernate)
- **AI Integration**: Spring AI with Ollama support

**Frontend:**
- **Framework**: Angular 21.2.7
- **Language**: TypeScript 5.9.3
- **Charts**: Chart.js 4.5.1 + ng2-charts 10.0.0
- **Styling**: CSS with responsive layouts
- **Icons**: FontAwesome 7.2.0

## Quick Start

Ensure all prerequisites were installed before building the project and mvn, npm and graalvm are in PATH.

Building the entire project as Mac dmg file, run the following command at root directory of this project:
````
sh build.sh
````

After completed, a new dmg file will be created at `desktop-app/release` folder.

The build time is approximately from 10 to 15 minutes.

## Features

### Core Features

#### Task Management
- Create, read, update, and delete tasks
- **Task Priority Levels**: Low, Medium, High, Critical with color-coded badges
- Categorize tasks by type (Study, Work, Health, etc.)
- Tag tasks with multiple customizable tags
- Track progress with current/target values
- Mark tasks as complete/incomplete

#### Tags & Task Types
- Create custom tags with colors for visual organization
- Create task types with icons
- Search tags by name
- Filter tasks by tag or type

#### Metrics Dashboard
- **Weekly/Monthly Metrics**: View study time, sessions, habit completion rates
- **Task Metrics by Tag**: Bar chart visualization showing completed vs pending tasks grouped by tag
- Tabs interface to switch between Task metrics and Other metrics

### Advanced Features

#### Monthly Overview
- View all tasks grouped by week within a selected month
- Month navigation (Previous/Current/Next)
- Tasks sorted alphabetically within each week
- Statistics summary (total, completed, pending counts)
- Tasks appear in all weeks they overlap with

#### Weekly Task Calendar
- Week-based calendar view (Monday-Sunday, 8 AM - 10 PM)
- Assign tags to specific day/hour slots
- **Multi-Tag Support**: Assign multiple tags to a single time slot
- **Multi-Day Assignment**: Assign tags to multiple days simultaneously
- Automatic task loading: Shows the highest priority incomplete task for assigned tags
- Task selection algorithm prioritizes by priority level (Critical > High > Medium > Low), then by start date
- Visual progress bars with color coding (green >70%, yellow 30-70%, red <30%)
- Week navigation and "Today" button

#### Database Backup & Restore
- Admin panel for database management
- Create on-demand backups (DB and SQL formats)
- List all available backups with metadata
- Restore from backup with confirmation dialog
- Toast notifications for operation results

### AI Features

The application integrates **Spring AI** with **Ollama** for AI-powered capabilities:

- **AI Model Support**: Uses Ollama for local AI model execution
- **Spring AI BOM**: Version 2.0.0-M4 for AI integration
- **Starter Dependency**: `spring-ai-starter-model-ollama`

*Note: AI features require Ollama to be installed and running locally.*

## Feature Specifications (PRDs)

Detailed specifications for each feature are available in the `/spec/features/` directory:

| Feature | PRD File |
|---------|----------|
| Monthly Overview | `monthly-overview-prd.md` |
| Monthly Task Metrics | `monthly-task-metrics.md` |
| Multi-Day Tag Assignment | `multi-day-tag-assignment.prd.md` |
| Task Alarm Notification | `task-alarm-notification.prd.md` |
| Task Priority Field | `task-priority-field.prd.md` |
| Task Metrics Dashboard | `task-metrics-dashboard.prd.md` |
| Tasks Weekly Grouping | `tasks-weekly-grouping-refactor.md` |
| Toast Migration | `toast-migration-all-features.prd.md` |
| Toast Notification System | `toast-notification.prd.md` |
| Weekly Task Calendar | `weekly-task-calendar.prd.md` |
| Database Backup & Restore | `database-backup-restore.prd.md` |
| Footer Component | `footer-component-prd.md` |

## Data access

All database data including the backups and H2 files can be found at $HOME/.task-stream-ai

### Application structure

The entire application is splited into 3 main projects:

1. **backend** - Spring Boot backend application
2. **desktop-app** - GraalVM native desktop application
3. **frontend** - Angular frontend application

You can find more details about each project in their respective README files:
- [backend/README.md](backend/README.md)
- [desktop-app/README.md](desktop-app/README.md)
- [frontend/README.md](frontend/README.md)