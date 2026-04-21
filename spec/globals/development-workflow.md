# AI Workflow: Pre-Implementation Protocol

## Overview
This document defines the mandatory steps that must be executed **before starting any implementation or refactoring task** during the AI-assisted software development process.

The goal is to ensure clarity, alignment, and correctness before writing or modifying code.

---

## Execution Rules

The AI MUST follow this sequence strictly before implementing any change.

---

## Step 1: Clarification Phase

Before starting, the AI must analyze the request and ask clarifying questions when necessary.

### Guidelines
- Ask questions if:
  - Requirements are ambiguous
  - Business rules are unclear
  - Technical constraints are missing
  - There are multiple possible approaches

- Avoid unnecessary questions if the request is already clear.

### Examples
- "Should this feature be accessible only to authenticated users?"
- "Do you want this implemented using REST or GraphQL?"
- "Should this follow the existing project architecture?"

---

## Step 2: Implementation Plan

After clarification (or if no clarification is needed), the AI must present a structured plan.

### The plan must include:

#### 1. Scope
- What will be implemented or changed
- Affected layers (frontend, backend, or both)

#### 2. Technical Approach
- Key design decisions
- Architecture patterns (if applicable)
- Data flow overview

#### 3. Components / Modules
- Frontend:
  - Components
  - Services
  - State management (if needed)

- Backend:
  - Controllers
  - Services
  - Repositories
  - DTOs / Models

#### 4. API Design (if applicable)
- Endpoints
- Request/response structure

#### 5. Risks / Considerations
- Edge cases
- Performance concerns
- Security implications

---

## Step 3: User Confirmation (must have)

- If the change is significant, the AI should ask for confirmation before proceeding.
- Example:
  - "Do you want me to proceed with this plan?"

---

## Step 4: Implementation

- Only start coding AFTER completing Steps 1–3.
- Follow project standards and defined persona guidelines.

---

## Step 5: Build & Validation (must have)

After implementation, the AI MUST ensure the project builds successfully.

### Requirements

#### Frontend (Angular 21)
- Run build process:
  ```bash
  npm run build

  # AI Workflow: Implementation Protocol

## Rule 0 (CRITICAL)
- DO NOT start coding immediately
- ALWAYS follow the steps below

---

## Step 1: Understand the Request
- Summarize the feature in 1–2 sentences
- Identify if anything is unclear

---

## Step 2: Clarification (must have)
Ask questions ONLY if:
- Requirements are ambiguous
- Business rules are missing
- Multiple valid approaches exist

---

## Step 3: Implementation Plan

### 1. Scope
- What will be built or changed

### 2. Technical Approach
- Key decisions
- Data flow

### 3. Components / Modules
- Frontend
- Backend

### 4. API Design (if applicable)

### 5. Risks / Edge Cases

---

## Step 4: Task Breakdown (MANDATORY)
Break the work into SMALL executable steps.

Rules:
- Each task must be independently implementable
- Avoid large or vague tasks

---

## Step 5: Self-Review
Validate:
- Does the plan cover all PRD requirements?
- Is it overengineered?
- Is it consistent with the project?

---

## Step 6: Confirmation (for complex tasks)
Ask:
"Do you want me to proceed with this plan?"

---

## Step 7: Implementation
- Follow the plan and tasks strictly
- Do not deviate without justification

---

## Step 8: Validation

Ensure:
- Build passes
- No type errors
- Code is consistent with standards