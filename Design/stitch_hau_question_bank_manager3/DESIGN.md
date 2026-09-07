---
name: HAU Academic Question Bank System
colors:
  surface: '#f5fafd'
  surface-dim: '#d5dbde'
  surface-bright: '#f5fafd'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#eff4f8'
  surface-container: '#e9eff2'
  surface-container-high: '#e3e9ec'
  surface-container-highest: '#dee3e7'
  on-surface: '#171c1f'
  on-surface-variant: '#3d484e'
  inverse-surface: '#2b3134'
  inverse-on-surface: '#ecf1f5'
  outline: '#6d797f'
  outline-variant: '#bdc8cf'
  surface-tint: '#006782'
  primary: '#006782'
  on-primary: '#ffffff'
  primary-container: '#0aa5ce'
  on-primary-container: '#003545'
  inverse-primary: '#5cd4ff'
  secondary: '#3c6473'
  on-secondary: '#ffffff'
  secondary-container: '#bfe9fb'
  on-secondary-container: '#426a79'
  tertiary: '#8a5100'
  on-tertiary: '#ffffff'
  tertiary-container: '#d58628'
  on-tertiary-container: '#492800'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#baeaff'
  primary-fixed-dim: '#5cd4ff'
  on-primary-fixed: '#001f29'
  on-primary-fixed-variant: '#004d62'
  secondary-fixed: '#bfe9fb'
  secondary-fixed-dim: '#a4cdde'
  on-secondary-fixed: '#001f29'
  on-secondary-fixed-variant: '#224c5b'
  tertiary-fixed: '#ffdcbd'
  tertiary-fixed-dim: '#ffb86f'
  on-tertiary-fixed: '#2c1600'
  on-tertiary-fixed-variant: '#693c00'
  background: '#f5fafd'
  on-background: '#171c1f'
  surface-variant: '#dee3e7'
typography:
  headline-xl:
    fontFamily: Inter
    fontSize: 2rem
    fontWeight: '600'
    lineHeight: 2.5rem
    letterSpacing: -0.025em
  headline-xl-mobile:
    fontFamily: Inter
    fontSize: 1.5rem
    fontWeight: '600'
    lineHeight: 2rem
    letterSpacing: -0.02em
  headline-lg:
    fontFamily: Inter
    fontSize: 1.5rem
    fontWeight: '600'
    lineHeight: 2rem
    letterSpacing: -0.02em
  headline-md:
    fontFamily: Inter
    fontSize: 1.25rem
    fontWeight: '600'
    lineHeight: 1.75rem
    letterSpacing: -0.015em
  headline-sm:
    fontFamily: Inter
    fontSize: 1.125rem
    fontWeight: '600'
    lineHeight: 1.5rem
    letterSpacing: -0.01em
  body-lg:
    fontFamily: Inter
    fontSize: 1rem
    fontWeight: '400'
    lineHeight: 1.5rem
    letterSpacing: -0.005em
  body-md:
    fontFamily: Inter
    fontSize: 0.875rem
    fontWeight: '400'
    lineHeight: 1.375rem
    letterSpacing: 0em
  body-sm:
    fontFamily: Inter
    fontSize: 0.75rem
    fontWeight: '400'
    lineHeight: 1.125rem
    letterSpacing: 0.005em
  label-md:
    fontFamily: Inter
    fontSize: 0.875rem
    fontWeight: '500'
    lineHeight: 1.25rem
    letterSpacing: -0.005em
  label-sm:
    fontFamily: Inter
    fontSize: 0.75rem
    fontWeight: '600'
    lineHeight: 1rem
    letterSpacing: 0.02em
  code-sm:
    fontFamily: JetBrains Mono
    fontSize: 0.8125rem
    fontWeight: '400'
    lineHeight: 1.25rem
    letterSpacing: 0em
rounded:
  sm: 0.125rem
  DEFAULT: 0.25rem
  md: 0.375rem
  lg: 0.5rem
  xl: 0.75rem
  full: 9999px
spacing:
  space-xxs: 0.125rem
  space-xs: 0.25rem
  space-sm: 0.5rem
  space-md: 0.75rem
  space-base: 1rem
  space-lg: 1.5rem
  space-xl: 2rem
  space-2xl: 3rem
  sidebar-width: 16.25rem
  gutter-desktop: 1.5rem
  gutter-tablet: 1rem
  gutter-mobile: 0.75rem
---

## Brand & Style

This design system serves as an enterprise-grade academic administration platform for Hanoi University of Architecture (HAU). The brand personality balances authoritative institutional rigor with Apple-inspired clarity, precision, and restrained aesthetic dignity.

### Tone & Sensibility
- **Architectural & Exact:** Clean alignments, intentional proportions, and purposeful grid geometry echoing the discipline of architectural drafting.
- **Academic Authority:** Grounded in tradition through a modern institutional cyan-blue core, while maintaining contemporary enterprise efficiency.
- **Cognitive Ease:** High-density data tables, question taxonomies, and approval workflows are rendered calm and transparent through disciplined whitespace and absence of ornamental excess.

### Visual Style
- **Corporate Minimal / Modern Enterprise:** Strictly eliminates decorative glassmorphism, heavy gradients, and non-semantic embellishments. 
- **Surface Contrast:** Relies on crisp structural boundaries, flat surfaces layered upon a tinted light canvas (`#F5F5F7`), and hairline borders (`rgba(0, 0, 0, 0.08)`).
- **Functional Semantics:** Colors communicate validation, status, and pedagogical metrics directly without distraction.

## Colors

The color palette anchors the system to a clean, professional cyan-blue identity while meeting WCAG 2.1 AAA contrast targets for enterprise data management.

### Palette Overview
- **Primary Institutional (`#0AA5CE`):** HAU Cyan. Applied to critical call-to-actions, active navigation states, verified question badges, and institutional mastheads.
- **Dark Slate Accent (`#557D8D`):** Pressed/hover states for primary elements, active navigation rails, and focal metrics.
- **Surface & Foundation:**
  - Base Application Background: `#F5F5F7` (Apple-standard light gray canvas)
  - Card & Container Surface: `#FFFFFF`
  - Subtle Border / Divider: `rgba(0, 0, 0, 0.08)` (Crisp micro-borders for modular cards and tables)
  - Focused Border: `rgba(10, 165, 206, 0.40)`
- **Typography & Neutrals:**
  - Primary Text: `#1D1D1F` (High legibility, zero glare)
  - Secondary / Supporting Text: `#6E6E73` (Metadata, breadcrumbs, table headers)
  - Muted / Disabled: `#A1A1A6`
- **Academic Semantics:**
  - **Success (Approved / Published):** Text/Icon `#2E7D32`, Surface Tint `#E8F5E9`
  - **Warning (Draft / Pending Review):** Text/Icon `#ED6C02`, Surface Tint `#FFF4E5`
  - **Danger (Rejected / Flagged Error):** Text/Icon `#D32F2F`, Surface Tint `#FFEBEE`
  - **Info (In Matrix / Blueprint Sync):** Text/Icon `#0288D1`, Surface Tint `#E1F5FE`

## Typography

The type hierarchy relies on `Inter` (with `SF Pro` fallbacks on Apple platforms) for high legibility in dense administrative layouts, supplemented by `JetBrains Mono` for question identifiers, formula codes, and matrix coordinates.

### Principles
- **Clarity over Flourish:** Purely typographic hierarchy driven by weight (600 semi-bold for headers, 500 medium for structural indicators, 400 regular for body data) and scale.
- **Tabular Figures:** All numeric displays (counts, difficulty percentages, Bloom taxonomy indexes) must enable OpenType tabular numbers (`tnum`) to maintain vertical alignment in data tables.
- **Mathematical & Code Integration:** Inline equations and question IDs (e.g., `HAU-CNTT-JAVA-042`) utilize monospaced styling to visually distinguish curriculum metadata from standard descriptive labels.

## Layout & Spacing

The layout is built on an architectural 8pt grid system. It enforces systematic, high-efficiency navigation paired with structured workspace modules.

### Structural Architecture
- **Master Admin Shell:** 
  - **Left Navigation Rail / Sidebar:** Fixed `16.25rem` (260px) width containing faculty hierarchy (Khoa CNTT, Kiến trúc, Xây dựng, Quy hoạch) and course taxonomies.
  - **Top Utility Header:** Fixed `3.5rem` (56px) height containing universal search, academic semester switcher, and user credential controls.
  - **Work Canvas:** Fluid canvas with a maximum container limit of `1600px` for ultra-wide displays to maintain ergonomic line lengths.
- **Grid & Alignment:**
  - **Desktop (≥ 1280px):** 12-column fluid grid, `1.5rem` gutter, `2rem` outer padding.
  - **Tablet (768px – 1279px):** 8-column layout, `1rem` gutter, sidebar collapses into an overlay drawer.
  - **Mobile (< 768px):** 4-column layout, `0.75rem` gutter, `1rem` screen margin; complex comparison matrices shift to stacked review drawers.

## Elevation & Depth

This system avoids heavy physical drop shadows or frosted glass artifacts, adhering to an intentional "low-contrast outline" and planar elevation model.

### Elevation Hierarchy
- **Level 0 (Canvas Base):** Flat `#F5F5F7` background.
- **Level 1 (Card & Module Layer):** Solid `#FFFFFF` surface enclosed by a hairline boundary: `border: 1px solid rgba(0, 0, 0, 0.08)`. Zero shadow in neutral state.
- **Level 2 (Hover / Active Cards & Action Drawers):** Elevated via an ultra-diffused, neutral ambient shadow: `box-shadow: 0 4px 16px -2px rgba(0, 0, 0, 0.06), 0 1px 2px rgba(0, 0, 0, 0.04)`.
- **Level 3 (Modals & Examination Blueprint Builder):** Floating modals centered with structured framing: `box-shadow: 0 12px 32px -4px rgba(0, 0, 0, 0.12), 0 2px 6px rgba(0, 0, 0, 0.04)`, bordered by `rgba(0, 0, 0, 0.12)`.

## Shapes

The design system employs precise, subtle radii that communicate clean engineering and modern precision without appearing bubbly or casual.

### Corner Radius Standards
- **Sub-components (Inputs, Buttons, Badges, Table Rows):** `0.25rem` (4px) to `0.375rem` (6px) for sharp structural cadence.
- **Containers (Cards, Filter Bars, Data Panels):** `0.5rem` (8px) boundary radius.
- **Modals & Flyouts:** `0.75rem` (12px) maximum.
- **Pills / Status Dots:** Fully rounded (`9999px`) reserved exclusively for circular indicators and status chips to distinguish them from actionable buttons.

## Components

### Buttons
- **Primary:** Solid HAU Cyan background (`#0AA5CE`), white text (`#FFFFFF`), `0.375rem` radius, subtle inner bevel border. Hover state deepens to `#557D8D`.
- **Secondary / Outline:** White background (`#FFFFFF`), border `1px solid rgba(0, 0, 0, 0.12)`, text `#1D1D1F`. Hover state uses `#F5F5F7`.
- **Destructive:** Soft background `#FFEBEE`, crimson text `#D32F2F`, border `1px solid rgba(211, 47, 47, 0.20)`. Used for irreversible actions such as deleting exam questions.
- **Sizing:** Compact `32px` height for table row actions; standard `40px` height for modal and page-level controls.

### Form Inputs & Search Fields
- **Fields:** Background `#FFFFFF`, border `1px solid rgba(0, 0, 0, 0.12)`, text `#1D1D1F`, placeholder `#6E6E73`, radius `0.375rem`. Focus ring uses an outline of `2px solid rgba(10, 165, 206, 0.25)` and border `#0AA5CE`.
- **Search Bar:** Prominent search bar in header with integrated keyboard shortcut indicator (`⌘K`), calibrated for instant querying across question stems, course codes, and author IDs.

### Data Tables (Academic Matrix)
- **Container:** Bordered white card with `0.5rem` outer radius.
- **Header:** Background `#FAFAFA`, bottom border `1px solid rgba(0, 0, 0, 0.08)`, uppercase label text `label-sm` (`#6E6E73`).
- **Rows:** Alternating hover highlight (`#F9F9FB`), row bottom border `1px solid rgba(0, 0, 0, 0.04)`, vertical alignment centered.
- **Action Columns:** Pinned right for quick actions (Chỉnh sửa, Phê duyệt, Xem trước LaTeX/Hình học họa hình).

### Status Badges & Academic Tags
- **Approved / Đã duyệt:** Background `#E8F5E9`, text `#2E7D32`, border `1px solid rgba(46, 125, 50, 0.2)`.
- **Pending Review / Chờ duyệt:** Background `#FFF4E5`, text `#ED6C02`, border `1px solid rgba(237, 108, 2, 0.2)`.
- **Draft / Bản nháp:** Background `#F5F5F7`, text `#6E6E73`, border `1px solid rgba(0, 0, 0, 0.08)`.
- **Bloom Taxonomy Indicators:** Micro tags showing cognitive depth: `Nhận biết` (L1), `Thông hiểu` (L2), `Vận dụng` (L3), `Vận dụng cao` (L4).

### Cards & Question Review Modules
- **Question Card:** Features header with Course Metadata (`Khoa CNTT • Lập trình Java` or `Khoa Kiến trúc • Hình học họa hình`), question ID in monospaced font, stem area supporting rich mathematical symbols/draft drawings, and multiple-choice answer radio slots clearly partitioned with light boundary borders.