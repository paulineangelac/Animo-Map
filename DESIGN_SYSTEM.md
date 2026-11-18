# Animo Map Design System

## 1. Product and Visual Identity

Animo Map is a campus companion application for De La Salle University Manila students. Its visual identity follows a modern, minimal dark-mode design with a charcoal and graphite foundation and teal accents.

The interface should feel clean, cohesive, and approachable. Use contrast, spacing, typography, and subtle surface variations to establish visual hierarchy rather than relying on excessive decoration.

Teal is the application's recognizable accent color. Use it selectively to emphasize interactive elements, selected states, and important actions.

## 2. Color Palette

Use the following colors consistently throughout the application.

| Token | HEX | Role |
|---|---|---|
| `bg_primary` | `#101316` | Main application background |
| `surface_card` | `#191E22` | Cards, panels, and content surfaces |
| `surface_elevated` | `#242A2F` | Raised surfaces and secondary controls |
| `border_subtle` | `#343B41` | Subtle borders and separators |
| `primary_teal` | `#2A6F73` | Primary actions and important accents |
| `accent_teal` | `#43CDBB` | Interactive icons, selected text, and active states |
| `highlight_selected` | `#A7F7DF` | Prominent highlights and selected-state details |
| `text_primary` | `#F0F3F4` | Main text and headings |
| `text_secondary` | `#9AA4AA` | Supporting text and metadata |

Maintain sufficient contrast between text and its background. Additional semantic colors may be introduced when necessary, provided they remain consistent with the overall visual identity.

## 3. Typography

Use Android's system sans-serif family or an existing visually similar sans-serif font. Avoid unnecessary font dependencies.

| Level | Size | Weight | Use |
|---|---:|---|---|
| H1 | 24–28sp | Bold | Main headings and screen titles |
| H2 | 20–22sp | Bold | Section headings |
| H3 | 16–18sp | SemiBold | Component and content titles |
| Body | 14–16sp | Regular | Main descriptions and content |
| Caption | 12–13sp | Regular or Medium | Supporting details and metadata |

Maintain a consistent hierarchy and readable line spacing. Use typography, rather than excessive color variation, to distinguish levels of information.

## 4. Spacing and Shapes

Use a consistent 4dp-based spacing scale:

- 4dp
- 8dp
- 12dp
- 16dp
- 20dp
- 24dp
- 32dp

Maintain consistent margins, padding, and spacing between related components.

Use rounded shapes throughout the application:

- 10–12dp for compact controls and smaller components.
- 14–16dp for standard buttons, cards, and containers.
- 20–24dp for larger panels and prominent components.
- Pill-shaped corners for search inputs and compact selection controls where appropriate.

Keep corner treatments consistent between related components.

## 5. Surfaces and Components

Use surface variations to establish depth and distinguish different content levels.

- `bg_primary` serves as the main application canvas.
- `surface_card` is used for primary content containers.
- `surface_elevated` distinguishes secondary or nested components.
- `border_subtle` provides understated separation when necessary.

Prefer subtle surface contrast and borders over heavy shadows or excessive elevation.

Components should have consistent spacing, alignment, corner treatments, and visual hierarchy. Avoid making every component look identical; its appearance should reflect its purpose and importance within the interface.

The bottom navigation should visually integrate with the application background rather than appearing as a separate, heavily elevated container.

## 6. Buttons, Inputs, and Selection Controls

Use consistent treatments for interactive components.

**Buttons**
- Primary buttons use the primary teal with readable text.
- Secondary buttons use neutral graphite surfaces.
- Maintain consistent sizing, padding, and corner treatments.

**Inputs**
- Use dark graphite surfaces with readable text and hints.
- Prefer clean, borderless appearances over unnecessary outlines.
- Use teal to indicate focus or active interaction when appropriate.

**Selection Controls**
- Use rounded chips or compact controls with clearly distinguishable selected and unselected states.
- Prefer changes in surface color and text color over prominent outlines.
- Use teal to emphasize selected labels and interactive states.
- Maintain consistent shapes and spacing between related controls.

All interactive elements should provide clear feedback and maintain accessible touch targets.

## 7. Icons and Navigation

Use simple, recognizable icons with consistent visual weight, sizing, and alignment.

- Teal identifies active or selected icons and important interactive elements.
- Muted gray is used for inactive icons.
- Notification and other secondary actions may use standalone icons without unnecessary background containers.
- Avoid mixing inconsistent icon styles.

The primary bottom navigation contains four destinations: Map, Community, Groups, and Profile.

Navigation should remain visually integrated with the application. Indicate the selected destination through its teal icon and label rather than additional outlined containers or filled selection indicators.

Keep navigation labels visible and maintain consistent alignment across destinations.

## 8. General UI Principles

- Maintain a cohesive charcoal and graphite dark-mode appearance.
- Use teal selectively as the application's recognizable accent.
- Prioritize clarity, readability, and visual hierarchy.
- Maintain consistent spacing, typography, component shapes, and alignment.
- Prefer understated borders and surface variations over excessive shadows.
- Use imagery where it contributes meaningful visual context.
- Avoid unnecessary gradients, neon effects, decorative elements, and excessive outlines.
- Keep components visually consistent while allowing their layouts and treatments to suit their intended purposes.
- Follow beginner-friendly and maintainable Android Java and XML implementation practices.

The design system provides the shared visual foundation for Animo Map. Individual screens and features may use different layouts and component arrangements while maintaining these common visual conventions.