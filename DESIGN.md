---
name: HAU Exam Bank Design System

description: A modern, Apple-inspired academic workspace for question bank management, AI-assisted content generation, exam matrix management, and administrative workflows.

designPhilosophy:
  northStar: "The Intelligent Academic Workspace"
  inspiration:
    - "Apple Human Interface principles: clarity, hierarchy, focus, restraint"
    - "Professional academic administration systems"
    - "Modern productivity applications"
  principles:
    - "Clarity over decoration"
    - "Content first"
    - "Quiet visual hierarchy"
    - "High usability for dense academic data"
    - "Consistent interaction patterns"
    - "Accessible by default"

colors:
  primary: "#111111"
  primary-hover: "#2C2C2E"
  primary-foreground: "#FFFFFF"

  neutral-bg: "#F5F5F7"
  surface: "#FFFFFF"
  neutral-fg: "#1D1D1F"
  secondary-fg: "#6E6E73"
  tertiary-fg: "#8E8E93"

  muted: "#F2F2F4"
  muted-hover: "#E8E8ED"

  accent: "#0071E3"
  accent-hover: "#0077ED"
  accent-soft: "#EAF3FF"

  success: "#248A3D"
  success-soft: "#EAF7ED"

  warning: "#B15C00"
  warning-soft: "#FFF4E5"

  danger: "#D70015"
  danger-soft: "#FFF0F0"

  info: "#0071E3"
  info-soft: "#EAF3FF"

  border: "rgba(0, 0, 0, 0.10)"
  border-strong: "rgba(0, 0, 0, 0.16)"
  divider: "rgba(60, 60, 67, 0.12)"

typography:
  fontFamily: "-apple-system, BlinkMacSystemFont, 'SF Pro Display', 'SF Pro Text', 'Inter', 'Segoe UI', Roboto, Helvetica, Arial, sans-serif"

  display:
    fontSize: "34px"
    fontWeight: 700
    lineHeight: 1.15
    letterSpacing: "-0.025em"

  headline:
    fontSize: "24px"
    fontWeight: 700
    lineHeight: 1.25
    letterSpacing: "-0.015em"

  title:
    fontSize: "17px"
    fontWeight: 600
    lineHeight: 1.35
    letterSpacing: "-0.005em"

  body:
    fontSize: "15px"
    fontWeight: 400
    lineHeight: 1.55
    letterSpacing: "normal"

  secondary:
    fontSize: "13px"
    fontWeight: 400
    lineHeight: 1.45

  label:
    fontSize: "13px"
    fontWeight: 600
    lineHeight: 1.2

rounded:
  xs: "6px"
  sm: "8px"
  md: "12px"
  lg: "16px"
  xl: "20px"
  pill: "999px"

spacing:
  xs: "4px"
  sm: "8px"
  md: "16px"
  lg: "24px"
  xl: "32px"
  xxl: "48px"

layout:
  sidebarWidth: "248px"
  sidebarCollapsedWidth: "72px"
  headerHeight: "64px"
  pageMaxWidth: "1600px"
  contentPaddingDesktop: "32px"
  contentPaddingTablet: "24px"
  contentPaddingMobile: "16px"

elevation:
  none: "none"
  subtle: "0 1px 2px rgba(0, 0, 0, 0.04)"
  floating: "0 8px 30px rgba(0, 0, 0, 0.10)"
  modal: "0 20px 60px rgba(0, 0, 0, 0.16)"

motion:
  fast: "120ms"
  normal: "180ms"
  slow: "260ms"
  easing: "cubic-bezier(0.2, 0.8, 0.2, 1)"

components:
  button-primary:
    backgroundColor: "{colors.primary}"
    textColor: "{colors.primary-foreground}"
    rounded: "{rounded.sm}"
    padding: "10px 16px"
    minHeight: "40px"

  button-secondary:
    backgroundColor: "{colors.muted}"
    textColor: "{colors.neutral-fg}"
    rounded: "{rounded.sm}"
    padding: "10px 16px"
    minHeight: "40px"

  button-accent:
    backgroundColor: "{colors.accent}"
    textColor: "#FFFFFF"
    rounded: "{rounded.sm}"
    padding: "10px 16px"

  card:
    backgroundColor: "{colors.surface}"
    borderColor: "{colors.border}"
    rounded: "{rounded.lg}"
    padding: "20px"
    shadow: "{elevation.none}"

  input:
    backgroundColor: "#FFFFFF"
    borderColor: "{colors.border}"
    rounded: "{rounded.sm}"
    minHeight: "42px"

  table:
    backgroundColor: "{colors.surface}"
    borderColor: "{colors.divider}"
    rounded: "{rounded.md}"

  badge:
    rounded: "{rounded.pill}"
    padding: "4px 8px"

  modal:
    backgroundColor: "{colors.surface}"
    rounded: "{rounded.xl}"
    shadow: "{elevation.modal}"

---

# Design System: HAU Exam Bank

## 1. Overview

**Creative North Star: "The Intelligent Academic Workspace"**

The HAU Exam Bank Design System is a modern academic workspace built for lecturers, subject administrators, system administrators, and AI-assisted content workflows.

The interface should feel calm, precise, modern, and highly usable. The visual direction is inspired by Apple's emphasis on clarity, hierarchy, restraint, and content-first interaction, while still supporting the high information density required by question banks, exam matrices, review workflows, dashboards, and AI-generated content.

The system must not feel like a marketing landing page. It is a professional application workspace.

### Key Characteristics

- **Calm and Focused**: Neutral surfaces, restrained accent colors, and strong information hierarchy.
- **Apple-Inspired Restraint**: Clean typography, generous spacing, subtle boundaries, and minimal decorative noise.
- **Academic Professionalism**: Designed for structured educational content and administrative workflows.
- **High Usability**: Dense information is organized through hierarchy, spacing, grouping, and filtering rather than excessive decoration.
- **Consistent Interaction**: Similar actions must look and behave consistently across every role and module.

---

## 2. Design Principles

### 2.1. Clarity First

Every screen must answer three questions immediately:

1. Where am I?
2. What information is most important?
3. What action can I take next?

Avoid unnecessary visual effects that compete with content.

### 2.2. Content Over Chrome

The UI should support the content instead of visually dominating it.

Use strong visual emphasis only for:

- Primary actions.
- Important statuses.
- Validation errors.
- Critical notifications.
- AI processing state.
- Review state.

### 2.3. Quiet Hierarchy

Hierarchy should come primarily from:

- Typography.
- Spacing.
- Alignment.
- Grouping.
- Contrast.
- Dividers.

Do not use many different colors or shadows to create hierarchy.

### 2.4. Dense but Breathable

The system contains complex academic data, so screens may be information-dense.

However:

- Avoid cramped table cells.
- Maintain clear spacing between sections.
- Keep filters visually grouped.
- Use drawers or sheets for secondary workflows.
- Use progressive disclosure for advanced options.

### 2.5. Consistency Over Novelty

Do not invent a new interaction style for each page.

All modules should share:

- Header behavior.
- Filter layout.
- Table structure.
- Modal structure.
- Form spacing.
- Button hierarchy.
- Status badge styling.
- Empty states.
- Loading states.
- Error states.

---

## 3. Color System

The interface uses a neutral Apple-inspired palette with a restrained blue accent.

### 3.1. Primary

**Near Black — `#111111`**

Used for:

- Primary buttons.
- Main navigation emphasis.
- Strong text emphasis.
- Selected high-priority actions.

Hover:

`#2C2C2E`

### 3.2. Accent

**System Blue — `#0071E3`**

Used for:

- Links.
- Selected filters.
- Interactive highlights.
- AI-related actions.
- Active navigation indicators.
- Focus states.

Accent must not dominate the page.

### 3.3. Neutral Surfaces

Main application background:

`#F5F5F7`

Primary content surface:

`#FFFFFF`

Muted surface:

`#F2F2F4`

Main text:

`#1D1D1F`

Secondary text:

`#6E6E73`

### 3.4. Semantic Colors

Success:

`#248A3D`

Warning:

`#B15C00`

Danger:

`#D70015`

Info:

`#0071E3`

Semantic colors should primarily appear in:

- Status badges.
- Inline feedback.
- Alerts.
- Small indicators.

Avoid filling large containers with saturated semantic colors.

### 3.5. The Accent Restraint Rule

Accent and semantic colors should normally occupy less than approximately 10% of the visible application surface.

Color is used as a signal, not decoration.

---

## 4. Typography

The interface uses a system-first font stack inspired by Apple's product typography.

```css
font-family:
  -apple-system,
  BlinkMacSystemFont,
  "SF Pro Display",
  "SF Pro Text",
  "Inter",
  "Segoe UI",
  Roboto,
  Helvetica,
  Arial,
  sans-serif;
```

Do not require proprietary Apple font files to be bundled with the application.

### Typography Hierarchy

**Display**

- 34px
- Weight 700
- Main dashboard/page heading.

**Headline**

- 24px
- Weight 700
- Major section headings.

**Title**

- 17px
- Weight 600
- Card heading, modal heading, data group title.

**Body**

- 15px
- Weight 400
- Standard application text.

**Secondary**

- 13px
- Weight 400
- Supporting metadata and descriptions.

**Label**

- 13px
- Weight 600
- Buttons, table headers, badges, form labels.

### Typography Rules

- Never use more than 3-4 text sizes on one screen unless necessary.
- Do not make table content smaller than 13px.
- Avoid excessive bold text.
- Use weight and spacing before adding color.
- Paragraph width should remain readable.

---

## 5. Layout System

### 5.1. Desktop Workspace

Default structure:

```text
+------------------------------------------------------+
| Sidebar | Header / Toolbar                           |
|         +--------------------------------------------+
|         |                                            |
|         | Main Content                               |
|         |                                            |
|         |                                            |
+------------------------------------------------------+
```

Recommended:

- Sidebar: 248px.
- Collapsed sidebar: 72px.
- Header: 64px.
- Desktop content padding: 32px.
- Tablet content padding: 24px.
- Mobile content padding: 16px.

### 5.2. Page Structure

Most management pages should follow:

```text
Page Title
Page Description / Breadcrumb
Primary Action

Filters / Search

Content
  - Table
  - Grid
  - Dashboard
  - Detail panel

Pagination / Footer actions
```

### 5.3. Maximum Width

Large dashboard/data pages may use nearly the full viewport.

Content-heavy forms should use narrower containers where appropriate.

Do not stretch long form fields to 1600px unnecessarily.

---

## 6. Navigation

Use a compact left sidebar for desktop.

### Sidebar Sections

Potential groups:

- Dashboard.
- Question Bank.
- AI Workspace.
- Exam Matrix.
- Exams.
- Notifications.
- Users.
- Administration.
- Settings.

Menu visibility must depend on role.

### Active Navigation

Use:

- Subtle background.
- Stronger text.
- Small accent indicator if useful.

Do not use:

- Large saturated blocks.
- Gradients.
- Heavy shadows.

### Role-Specific Navigation

`SYSTEM_ADMIN`

Focus:

- Users.
- Accounts.
- Faculty assignment.
- System management.

`SUBJECT_ADMIN`

Focus:

- Question review.
- Question bank.
- Subjects.
- Exam matrix.
- Faculty-scoped content.

`USER`

Focus:

- My Questions.
- AI Generation.
- Upload Documents.
- Exam content where allowed.
- Notifications.

---

## 7. Cards and Containers

Cards should be quiet structural elements.

Default:

```text
background: white
border: 1px solid rgba(0, 0, 0, 0.10)
border-radius: 16px
shadow: none
```

Cards may use subtle shadow only when visually necessary.

### Do

Use cards for:

- KPI groups.
- AI jobs.
- Question summaries.
- Content grouping.
- Profile information.

### Don't

Do not put every small text block inside a card.

Avoid nested cards unless hierarchy genuinely requires it.

---

## 8. Buttons

### Primary Button

Use for the single most important action in the current context.

Examples:

- Create Question.
- Submit for Review.
- Generate with AI.
- Save Changes.
- Approve.

Style:

```text
Background: #111111
Text: white
Radius: 8px
Height: 40px+
```

### Accent Button

Blue buttons may be used for AI-oriented or clearly interactive actions where appropriate.

Do not mix black and blue primary actions randomly.

### Secondary Button

Muted neutral surface.

Used for:

- Cancel.
- Filter.
- Preview.
- Secondary commands.

### Destructive Button

Danger color should be used only for truly destructive actions.

Always require confirmation for destructive irreversible operations.

---

## 9. Forms

Inputs should feel clean and native.

Default:

```text
Background: white
Border: 1px solid rgba(0,0,0,0.10)
Radius: 8px
Min height: 42px
```

Focus:

- Accent blue ring.
- Stronger border.
- No aggressive glow.

### Form Layout

Prefer:

```text
Label
Input
Helper/Error
```

Forms should be grouped into meaningful sections.

Examples:

- Account Information.
- Personal Information.
- Faculty Information.
- Security.
- Question Content.
- Answer Configuration.

---

## 10. Tables

Tables are a major component of the system.

They should be:

- Dense.
- Readable.
- Stable.
- Easy to filter.
- Easy to scan.

### Table Rules

- Header text: 13px semibold.
- Body: 13-15px.
- Row height should normally stay around 48-56px.
- Use subtle row dividers.
- Hover should be barely visible.
- Selected rows may use accent-soft background.
- Sticky headers are recommended for long tables.

### Actions

Avoid displaying 5-6 action buttons directly inside every row.

Prefer:

```text
Primary contextual action
+
More (...) menu
```

---

## 11. Status Badges

Use pill-shaped compact badges.

Examples:

```text
DRAFT
PENDING REVIEW
APPROVED
NEED REVISION
REJECTED
ARCHIVED

AI PROCESSING
AI COMPLETED
AI FAILED
```

Use semantic color softly:

```text
light background
+
dark semantic text
```

Avoid saturated solid badge backgrounds unless necessary.

---

## 12. Dashboard

Dashboard should behave like an academic command center.

Recommended sections:

- Question statistics.
- Review queue.
- AI jobs.
- Recent activity.
- Exam matrix status.
- Notifications.
- Faculty-level statistics where applicable.

### KPI Cards

KPI cards must remain simple.

Example:

```text
Questions
1,248

+38 this month
```

Avoid decorative illustrations in KPI cards unless useful.

---

## 13. Question Bank UI

Primary pattern:

```text
Question Bank
  |
  +-- Search
  +-- Faculty filter
  +-- Subject filter
  +-- Chapter filter
  +-- Topic filter
  +-- Difficulty filter
  +-- Status filter
```

Question rows/cards should quickly expose:

- Short question preview.
- Subject.
- Chapter/topic.
- Difficulty.
- Status.
- Created by.
- AI/manual source.
- Last update.

Opening a question should show a clear detail workspace.

---

## 14. Question Editor

Use a focused editor layout.

Suggested structure:

```text
Question Content
Question Image

Answer Options

Correct Answer

Subject / Chapter / Topic

Difficulty
Question Type

AI Metadata
```

For answer options:

```text
A
B
C
D
```

Each option can optionally contain:

- Text.
- Image.

Do not overwhelm the screen with advanced metadata by default.

---

## 15. Review Workspace

SUBJECT_ADMIN review view should be optimized for decisions.

Recommended layout:

```text
Question Detail

Source / Author
Faculty
Subject
Chapter
Topic

Question
Answers
Correct Answer
Explanation

AI Metadata

Review History

[Request Revision] [Reject] [Approve]
```

The approval buttons should stay visually easy to find.

---

## 16. Exam Matrix UI

Exam Matrix requires structured grid design.

Example:

| Chapter | Easy | Medium | Hard | Total |
|---|---:|---:|---:|---:|
| Chapter 1 | 5 | 3 | 2 | 10 |
| Chapter 2 | 4 | 4 | 2 | 10 |

Use:

- Inline numeric controls.
- Immediate totals.
- Validation feedback.
- Coverage summary.

Do not hide important distribution information inside modals.

---

## 17. AI Workspace

AI features should feel integrated into the product, not like a completely separate application.

Suggested sections:

- Upload Document.
- Processing Jobs.
- Generate Questions.
- AI Suggestions.
- Chatbot.

### AI Action Pattern

Use accent blue selectively for:

- Generate.
- Regenerate.
- Ask AI.
- Analyze.
- AI suggestions.

### Processing

Always show clear job state:

```text
Uploading
Processing
Generating
Completed
Failed
```

Use progress indicators only when progress is meaningful.

Never show fake percentage progress if backend does not provide actual progress.

---

## 18. Chatbot UI

Chatbot should be restrained and productivity-oriented.

Avoid overly playful chat bubbles.

Suggested layout:

```text
Context / source selector
Conversation area
Composer
Source references
```

AI and user messages should be visually distinct but not highly decorative.

Generated answers should support:

- Markdown.
- Lists.
- References.
- Copy action.
- Feedback action if implemented.

---

## 19. Notifications

Notification center should support:

- Unread state.
- Type.
- Timestamp.
- Reference target.
- Mark as read.
- Mark all as read.

Realtime notification toast:

- Compact.
- Temporary.
- Non-blocking.

Important notifications may also exist in the persistent notification center.

---

## 20. Modals, Sheets, and Popovers

Use modal only for focused tasks.

Good modal use:

- Confirmation.
- Small forms.
- Review comments.
- Account locking.
- Delete confirmation.

Use side sheets/drawers for:

- Question preview.
- Filters on smaller screens.
- Secondary details.

Floating surfaces may use elevation.

Do not add heavy shadow to normal page content.

---

## 21. Elevation

### Flat by Default

Main content:

```text
No shadow
or
very subtle 0 1px 2px shadow
```

Floating surfaces:

- Dropdowns.
- Popovers.
- Tooltips.
- Modals.
- Command palette.

may use stronger shadow.

### Rule

If border already defines the surface clearly, do not add a large shadow.

---

## 22. Rounded Corners

Apple-inspired does not mean making everything extremely rounded.

Recommended:

```text
Input/Button: 8px
Table/Card: 12-16px
Modal: 20px
Badge: pill
```

Avoid excessive `24px-32px` rounding on normal workspace cards.

---

## 23. Motion

Motion should communicate state, not decorate.

Recommended duration:

```text
120ms - quick feedback
180ms - standard transition
260ms - modal/sheet transition
```

Use subtle:

- Fade.
- Scale 0.98 -> 1.
- Small translate.

Do not use:

- Bouncing elements.
- Long page animations.
- Decorative motion.
- Parallax.

Respect `prefers-reduced-motion`.

---

## 24. Loading States

Use:

- Skeleton for structured content.
- Spinner for small actions.
- Progress state for AI jobs.

Do not block the whole screen for a small API request.

Buttons performing actions should show loading state and prevent accidental duplicate submission.

---

## 25. Empty States

Empty states should explain what happened and provide one useful next action.

Example:

```text
No questions yet

Create your first question manually or generate questions from a document.

[Create Question]
[Generate with AI]
```

Avoid large decorative illustrations unless they genuinely help.

---

## 26. Error States

Errors must be clear, specific, and recoverable.

Use:

```text
Title
Short explanation
Suggested next action
```

Validation errors should appear near the relevant field.

Do not expose raw backend exceptions.

---

## 27. Accessibility

Minimum expectations:

- WCAG AA contrast for normal text.
- Keyboard navigation.
- Visible focus indicators.
- Semantic HTML.
- Form labels.
- Accessible modal focus trap.
- ARIA labels where needed.
- Icon-only buttons must have accessible names.
- Status must not rely on color alone.

---

## 28. Responsive Design

Primary target:

**Desktop web application**

The system must still work on tablet and mobile widths.

On smaller screens:

- Sidebar becomes drawer.
- Tables may become scrollable.
- Advanced filters move to a filter sheet.
- Cards become single-column.
- Secondary actions collapse into menus.

Do not force desktop table layouts into narrow screens without horizontal overflow handling.

---

## 29. Icons

Use one consistent icon library.

Recommended:

- Lucide.
- Phosphor.

Do not mix several icon styles.

Default icon size:

```text
16px
18px
20px
```

Avoid oversized icons in professional data screens.

---

## 30. Charts

Charts should be minimal and information-first.

Rules:

- Use few colors.
- Label important values.
- Avoid 3D charts.
- Avoid gradients.
- Avoid excessive legends.
- Prefer bar, line, donut, or simple distribution charts.

---

## 31. Light Mode

Light mode is the primary visual mode.

Base:

```text
App Background: #F5F5F7
Surface: #FFFFFF
Text: #1D1D1F
```

---

## 32. Dark Mode

Dark mode may be added after core UI is stable.

Do not implement dark mode by simply inverting all colors.

Use semantic tokens.

Suggested foundation:

```text
Background: #000000
Surface: #1C1C1E
Secondary Surface: #2C2C2E
Main Text: #F5F5F7
Secondary Text: #A1A1A6
```

---

## 33. Apple-Inspired Rules

The system is inspired by Apple's interface philosophy, but must remain its own product.

### Do

- Use strong typography hierarchy.
- Use generous whitespace.
- Keep controls visually quiet.
- Use subtle transitions.
- Use neutral surfaces.
- Use blue as a restrained interactive accent.
- Keep important content visually dominant.
- Use blur/translucency only when it improves spatial understanding.

### Don't

- Do not clone macOS or iOS screens directly.
- Do not copy Apple application layouts one-to-one.
- Do not overuse translucent glass surfaces.
- Do not use giant pill-shaped cards everywhere.
- Do not use decorative gradients across management pages.
- Do not turn a data-heavy admin interface into a marketing page.

---

## 34. Material / Transparency Rule

Subtle translucency may be used only for temporary or fixed navigation surfaces such as:

- Top navigation.
- Floating toolbar.
- Modal backdrop.

Example:

```css
background: rgba(255,255,255,0.80);
backdrop-filter: blur(20px);
```

Do not use glassmorphism for every card.

Normal application cards remain opaque.

---

## 35. Role-Based UI

Frontend visibility must reflect backend authorization, but frontend hiding is not security.

### SYSTEM_ADMIN

Primary UI:

- User Management.
- Registration Approval.
- Role Management.
- Faculty Assignment.
- System Overview.

### SUBJECT_ADMIN

Primary UI:

- Review Queue.
- Question Bank.
- Exam Matrix.
- Faculty Statistics.
- AI content review where appropriate.

### USER

Primary UI:

- My Questions.
- Create Question.
- Upload Document.
- AI Generation.
- Chatbot.
- Notifications.

Frontend must never assume hidden actions are secure; backend remains authoritative.

---

## 36. Component Naming

Components should use clear names.

Examples:

```text
PageHeader
SectionHeader
StatCard
DataTable
FilterBar
SearchInput
StatusBadge
EmptyState
ConfirmDialog
QuestionEditor
QuestionPreview
ReviewPanel
ExamMatrixEditor
NotificationPopover
AiJobCard
ChatComposer
```

Avoid meaningless names such as:

```text
Box1
Card2
WrapperX
ComponentNew
```

---

## 37. Frontend Agent Rules

When an AI Agent implements the frontend, it must:

1. Read this design system first.
2. Reuse existing components before creating new ones.
3. Follow the design tokens instead of hard-coded random colors.
4. Keep role-based screens consistent.
5. Use responsive behavior.
6. Implement loading, empty, success, and error states.
7. Maintain accessibility.
8. Avoid decorative gradients unless specifically approved.
9. Avoid heavy shadows on workspace cards.
10. Avoid excessive rounding.
11. Keep page hierarchy consistent.
12. Keep backend authorization rules in mind.
13. Do not invent new roles.
14. Do not expose features from another faculty to SUBJECT_ADMIN.
15. Do not redesign completed modules without explicit instruction.

---

## 38. Recommended Frontend Structure

```text
src/
|
+-- app/
|   +-- router/
|   +-- providers/
|   +-- layouts/
|
+-- components/
|   +-- ui/
|   +-- shared/
|   +-- layout/
|
+-- features/
|   +-- auth/
|   +-- users/
|   +-- questions/
|   +-- exams/
|   +-- ai/
|   +-- notifications/
|
+-- services/
|   +-- api/
|   +-- websocket/
|
+-- hooks/
|
+-- stores/
|
+-- types/
|
+-- utils/
|
+-- constants/
|
+-- styles/
    +-- tokens.css
    +-- globals.css
```

Features should own their business-specific components.

Reusable primitive components belong in `components/ui`.

---

## 39. Page Template

Most management pages should follow this template:

```text
<Page>
  <PageHeader
    title
    description
    breadcrumb
    primaryAction
  />

  <FilterBar />

  <MainContent>
    DataTable / Grid / Editor
  </MainContent>
</Page>
```

Do not invent a dramatically different page shell for each feature.

---

## 40. Design Review Checklist

Before considering a screen complete:

```text
[ ] Clear page title
[ ] Clear primary action
[ ] Proper role visibility
[ ] Proper faculty data scope representation
[ ] Loading state
[ ] Empty state
[ ] Error state
[ ] Responsive behavior
[ ] Keyboard accessibility
[ ] Focus states
[ ] Consistent typography
[ ] Consistent spacing
[ ] Semantic colors only
[ ] No unnecessary gradients
[ ] No heavy card shadows
[ ] No excessive corner rounding
[ ] Tables remain readable
[ ] Destructive actions require confirmation
```

---

## 41. Final Design Rule

The product should feel like:

> A quiet, intelligent, modern academic workspace where the interface disappears behind the work.

It should not feel like:

> A colorful dashboard template assembled from unrelated UI kits.

Every visual decision must support clarity, speed, trust, and academic professionalism.
