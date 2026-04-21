---
version: 1.0.0
last_updated: 2026-04-21
author: AI / Developer
---

# TaskStream AI Desktop

An Electron-based desktop application wrapper for TaskStream AI, bundling the GraalVM native backend and Angular frontend into a native macOS desktop experience.

## Overview

TaskStream AI Desktop packages the Spring Boot backend (compiled as a GraalVM native binary) with an Electron shell to provide a standalone desktop application. The backend binary auto-starts on launch, serving the web UI which loads in a native window.

**Target Audience:** End users who prefer a native desktop application over browser-based access.

## Architecture

### Tech Stack

| Layer | Technology |
|-------|------------|
| Shell | Electron 41 |
| Packager | electron-builder 26 |
| Backend | GraalVM Native Binary |
| Port | 1234 (internal) |
| Target | macOS DMG |

### Project Structure

```
desktop-app/
├── builds/              # GraalVM binary output directory
├── icons/              # Application icons (.icns)
├── release/            # DMG output directory
├── loading.gif         # Splash screen animation
├── main.js             # Electron main process
├── package.json        # Electron configuration
└── splash.html         # Loading screen HTML
```

## Installation

### Prerequisites

- Node.js 18+
- GraalVM native binary built from `../backend`

## Usage

### Development Mode

```bash
npm start
```

Launches the desktop app with splash screen and auto-starting backend on port 1234.

### Build macOS DMG

```bash
npm run build:mac
```

Output: `release/TaskStream AI-x.x.x.dmg`

### Full Build (Backend + Desktop)

run the script ***build.sh*** at the root folder to build both the backend and the desktop app.

## Development

### How It Works

1. **main.js** spawns the GraalVM binary as a child process
2. **Splash screen** displays for 5 seconds during backend startup
3. **Auto-retry mechanism** polls `http://localhost:1234` until backend is ready
4. **Main window** loads the web UI once backend responds
5. **Cleanup** kills backend process on window close

### Key Files

| File | Purpose |
|------|---------|
| `main.js` | Electron main process, backend lifecycle |
| `package.json` | Build config, dependencies, macOS signing |
| `splash.html` | Loading screen with animation |

### Build Configuration

```json
{
  "appId": "br.com.taskstream",
  "productName": "TaskStream AI",
  "extraResources": [{
    "from": "builds/task-stream-ai",
    "to": "backend-binary"
  }]
}
```

### Code Standards

- Use `contextIsolation: true` for security
- Backend path resolution differs between dev and packaged modes
- Always terminate backend process on app quit

## Configuration

### Environment Variables

None required. Backend runs on fixed port 1234 internally.

### Packaged App Paths

- **Dev mode:** `builds/task-stream-ai`
- **Packaged:** `Resources/backend-binary` (within .app)