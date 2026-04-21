# Documentation Guidelines

## Overview
This document defines guidelines and templates for creating, updating, and standardizing software documentation, including **README.md**, **PRDs (Product Requirement Documents)**, and **release notes**.  
The goal is to ensure clarity, consistency, and reusability, and to serve as a reference for AI assistants supporting documentation tasks.

---

## 1. README.md Guidelines

### 1.1 Recommended Structure
The README.md should include the following sections, where applicable:

1. **Project Title**
   - Project name
   - Logo or badges (optional)

2. **Overview**
   - Brief description of the project
   - Problem it solves
   - Target audience

3. **Features**
   - List of main features
   - Links to corresponding PRDs (if available)

4. **Architecture / Tech Stack**
   - Technologies used
   - Simplified diagram (if applicable)
   - Key patterns (e.g., MVC, microservices, event-driven)

5. **Installation / Setup**
   - Step-by-step instructions for running the project locally
   - Dependencies and prerequisites

6. **Usage**
   - Examples of usage
   - Screenshots, commands, or endpoints

7. **Contributing**
   - Link to contribution guidelines
   - How to open issues or submit pull requests

8. **Testing**
   - How to run tests
   - Tools used

9. **Changelog / Release Notes**
   - Brief version history
   - Links to detailed notes (if available)

10. **License**
    - Project license type

---

### 1.2 Writing Rules
- Clear, concise, and human-readable language
- Use active voice
- Prefer simple markdown for maximum compatibility
- Highlight code, commands, and key files with code blocks
- Keep links up to date
- Reference PRDs whenever possible

---

## 2. PRD (Product Requirement Document) Guidelines

### 2.1 Recommended Structure
Each PRD should contain:

1. **Feature Name**
2. **Overview / Objective**
3. **Target Users**
4. **Functional Requirements**
5. **Non-Functional Requirements**
6. **Technical Requirements**
7. **UI / UX (if applicable)**
8. **Success Metrics**
9. **Edge Cases / Constraints**
10. **Implementation Notes**
    - Files affected
    - Components / APIs / Services involved

---

### 2.2 Writing Rules
- Clear, precise, and understandable for both humans and AI
- Include sample data, endpoints, and response formats
- Avoid ambiguity in technical requirements
- Always relate the feature to product goals

---

## 3. Release Notes Guidelines

### 3.1 Recommended Structure
- **Version:** x.y.z
- **Date:** YYYY-MM-DD
- **Summary:** brief description of the release
- **New Features**
- **Improvements**
- **Bug Fixes**
- **Known Issues / Limitations**

### 3.2 Writing Rules
- Short and objective text
- Highlight important changes
- Reference corresponding PRDs or issues

---

## 4. AI Usage Guidelines

When AI is used to generate or update documentation:

1. **Always refer to this document** for consistency
2. For updating README.md:
   - Add only new or changed information
   - Preserve existing style and formatting
   - Include links to corresponding PRDs
3. For creating new PRDs:
   - Use the PRD template above
   - Include all mandatory sections
   - Generate endpoint examples or data models when applicable
4. For release notes:
   - Always link to implemented PRDs or issues
   - Use consistent version naming

---

## 5. Version Control / Metadata

To track documentation changes:

- Optional front-matter at the top of each md:

```yaml
---
version: 1.0.0
last_updated: 2026-04-05
author: AI / Developer
---