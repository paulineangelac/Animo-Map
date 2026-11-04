# AGENTS.md — ANIMO MAP

## 1. PROJECT IDENTITY

Animo Map is an Android mobile application for De La Salle University Manila students.

It combines three main functions:

- **Campus Navigation:** Help students locate campus buildings, classrooms, facilities, food establishments, and other points of interest. Provide campus directions and location information.
- **Community:** Allow students to share recommendations, reviews, photos, ratings, and comments about campus locations.
- **Groups:** Allow students to create private groups, coordinate with friends, and optionally share their campus building presence.

The application has three main sections: Map, Community, and Groups, connected through a shared bottom navigation bar.

Animo Map is intended to be developed as one complete, integrated Android application.

For detailed features, functional requirements, project scope, and constraints, refer to `PROJECT_DESCRIPTION.md`. Treat this document as the main project specification.

## 2. TECHNOLOGY

This is an academic project for ITISMOB (Mobile Development) at De La Salle University.

- IDE: Android Studio
- Programming Language: Java
- UI: XML layouts and Android Views
- Build System: Gradle Kotlin DSL
- Minimum SDK: API 24
- Package: `com.itismob.s02.group5.animomap`

Follow the existing repository configuration. Do not change technologies, SDK versions, or project architecture without approval.

## 3. ITISMOB COURSE ALIGNMENT

Implement features using concepts taught throughout the ITISMOB course.

The syllabus covers the following topics in sequence:

1. **Introduction to Android Development:** Android architecture, Android Studio setup and IDE familiarity, application structure, and debugging tools.
2. **Layouts and Basic UI Components:** XML layouts, Views, and Layouts.
3. **Activities:** Activity lifecycle, multiple Activities, and passing data using Intents.
4. **Data-Driven Views:** RecyclerView, Adapter, ViewHolder, and LayoutManager.
5. **Navigation and SharedPreferences:** Options Menu, application navigation, and storing data using SharedPreferences.
6. **Local Storage:** SQLite databases and CRUD operations using SQLiteHelper.
7. **Project Prototyping:** Creating the UI of the proposed application.
8. **Networking and JSON Parsing:** Fetching API data, parsing JSON, and displaying results in the application.

Prefer these concepts and beginner-friendly implementations whenever appropriate.

More advanced technologies and external services may be used when required by the project, but explain their purpose and necessity before introducing them.

Write code that the developers can understand, explain, and maintain.

## 4. OFFICIAL VISUAL IDENTITY

Animo Map uses a modern, minimal dark-mode design with a teal and blue-green accent palette.

Dark mode is the default application theme.

### Official Color Palette

Use these exact colors consistently throughout the application:

- **Primary Background:** `#0B1E26`
  Main screen backgrounds.

- **Surface / Card:** `#132F3A`
  Cards, panels, and content containers.

- **Elevated Surface:** `#1C3E46`
  Elevated cards and interactive surfaces.

- **Primary Teal:** `#2A6F73`
  Primary buttons and important UI elements.

- **Accent Teal:** `#43CDBB`
  Interactive accents, icons, and active navigation indicators.

- **Highlight / Selected:** `#A7F7DF`
  Selected states and prominent highlights.

- **Primary Text:** `#E6F7F5`
  Main text and headings.

- **Secondary Text:** `#8FAFB2`
  Supporting text, descriptions, and metadata.

Do not randomly introduce alternative colors. Additional colors may be used when functionally necessary, provided they remain visually consistent with the design system.

### Typography

Use Poppins or a visually similar sans-serif font.

Follow this typography hierarchy:

- H1: 24–28sp, Bold.
- H2: 20–22sp, Bold.
- H3: 16–18sp, SemiBold.
- Body: 14–16sp, Regular.
- Caption: 12sp, Regular.

### General Design Rules

- Modern, minimal, and clean appearance.
- Rounded cards, buttons, and containers.
- Consistent spacing and alignment.
- Clear visual hierarchy and readable text.
- Simple and recognizable icons.
- Teal accents for important actions and selected states.
- Avoid excessive gradients, neon effects, and unnecessary decoration.
- Maintain a consistent appearance across all application screens.

### Wireframes and Design References

The user may provide screen-specific wireframes, mockups, screenshots, or other visual references when requesting UI implementation.

Follow the supplied references for the intended screen layout, component placement, hierarchy, and interactions.

Apply the official Animo Map color palette, typography, and visual identity to those layouts.

Do not independently redesign screens or invent major UI elements when a reference is provided.

The attached design references take priority over design assumptions, unless told otherwise.

## 5. DEVELOPMENT APPROACH

**Develop incrementally, one feature at a time.**

- Inspect existing code before making changes.
- Implement only what the current task requests.
- Preserve existing functionality and navigation.
- Reuse existing classes, layouts, and components whenever possible.
- Avoid unnecessary dependencies and complex architecture.
- Do not modify unrelated screens or files.
- Do not implement future features unless requested.
- Do not overwrite existing work.

Keep implementations simple, readable, and appropriate for a beginner Android developer.

**Overall priority:** Build a functional, cohesive Animo Map application that follows the project requirements, official visual identity, supplied wireframes, and ITISMOB course concepts. Work progressively and avoid unnecessary complexity.