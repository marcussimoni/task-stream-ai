---
version: 1.0.0
last_updated: 2026-04-21
author: AI / Developer
---

# TaskStream AI Frontend

A modern Angular application for task management with scheduling, tagging, and productivity metrics.

## Overview

TaskStream AI Frontend provides a task management interface with weekly calendar scheduling, tag-based organization, and productivity analytics. It integrates with a Spring Boot backend for real-time task tracking and metrics.

**Target Audience:** Users seeking structured task management with scheduling and productivity insights.

## Features

| Feature | Description |
|---------|-------------|
| **Task Management** | Create, edit, and organize tasks with priorities and due dates |
| **Weekly Calendar** | Schedule and view tasks in a weekly calendar layout |
| **Tag System** | Categorize tasks with custom tags for better organization |
| **Monthly Overview** | Review completed tasks and productivity by month |
| **Metrics Dashboard** | Visualize task completion rates and productivity trends |
| **Admin Panel** | Application logs and backend health monitoring |

## Architecture

### Tech Stack

| Layer | Technology |
|-------|------------|
| Framework | Angular 21 (Standalone Components) |
| Language | TypeScript 5.x |
| State Management | RxJS + Angular Signals |
| HTTP Client | Angular HttpClient |
| Styling | SCSS, CSS Grid, Flexbox |
| Testing | Jasmine, Karma |

### Project Structure

```
src/
├── app/
│   ├── core/                    # Core services, models, interceptors
│   │   ├── models/             # Domain models (Task, Tag, Metrics, etc.)
│   │   ├── services/           # API services (REST)
│   │   └── interceptors/      # HTTP interceptors
│   ├── shared/                 # Shared components and modules
│   │   ├── components/        # Reusable UI components
│   │   ├── models/            # Shared models
│   │   └── services/          # Shared services
│   └── features/              # Feature modules
│       ├── admin/             # Admin panel (logs, health checks)
│       ├── metrics/           # Productivity analytics
│       ├── monthly-overview/  # Monthly task summaries
│       ├── tags/              # Tag management
│       ├── tasks/             # Task CRUD operations
│       └── weekly-calendar/   # Weekly schedule view
├── app.component.ts          # Root component (standalone)
├── app-routing.module.ts     # Route definitions
├── app.module.ts             # Root module
├── assets/                   # Static assets
└── styles.css                # Global styles
```

## Installation

### Prerequisites

- Node.js 20 LTS
- npm 10+ or yarn 1.22+
- Angular CLI 21 (`npm install -g @angular/cli`)

### Setup

1. Install dependencies:
   ```bash
   npm install
   ```

2. Configure the API proxy in `proxy.conf.json` (development):
   ```json
   {
     "/api": {
       "target": "http://localhost:8080",
       "secure": false
     }
   }
   ```

3. Start the development server:
   ```bash
   ng serve
   ```

4. Navigate to `http://localhost:4200/`

## Usage

### API Integration

The application expects a REST backend at the configured `apiUrl`. Ensure your Spring Boot server is running and accessible.

**Base Endpoint:** `/api`

| Resource | Endpoint | Operations |
|----------|----------|------------|
| Tasks | `/api/tasks` | GET, POST, PUT, DELETE |
| Tags | `/api/tags` | GET, POST, PUT, DELETE |
| Week Schedule | `/api/week-schedule` | GET, POST |
| Metrics | `/api/metrics` | GET |
| Alarms | `/api/alarms` | GET, POST, DELETE |

### Building for Production

```bash
npm run build-prod
```

Output is generated in `backend/src/main/resources/static`.

## Development

### Code Standards

- **Architecture:** Standalone components with lazy-loaded feature modules
- **State:** Prefer Angular Signals for local state, RxJS for async streams
- **HTTP:** Use `HttpClient` with interceptors for auth/error handling
- **Types:** Strict TypeScript; explicit return types on public methods
- **Styling:** SCSS with BEM methodology; mobile-first responsive design