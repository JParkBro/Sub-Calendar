---
name: Calm Ledger
colors:
  surface: '#f8f9ff'
  surface-dim: '#d5dae7'
  surface-bright: '#f8f9ff'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#eff4ff'
  surface-container: '#e9eefb'
  surface-container-high: '#e3e8f5'
  surface-container-highest: '#dde3ef'
  on-surface: '#161c25'
  on-surface-variant: '#454654'
  inverse-surface: '#2b313a'
  inverse-on-surface: '#ebf1fe'
  outline: '#757686'
  outline-variant: '#c5c5d7'
  surface-tint: '#3c4ed5'
  primary: '#1f33be'
  on-primary: '#ffffff'
  primary-container: '#3d4fd6'
  on-primary-container: '#d4d7ff'
  inverse-primary: '#bcc2ff'
  secondary: '#a33e00'
  on-secondary: '#ffffff'
  secondary-container: '#fd6604'
  on-secondary-container: '#541d00'
  tertiary: '#00541d'
  on-tertiary: '#ffffff'
  tertiary-container: '#006f28'
  on-tertiary-container: '#69f581'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#dfe0ff'
  primary-fixed-dim: '#bcc2ff'
  on-primary-fixed: '#000b62'
  on-primary-fixed-variant: '#1e32bd'
  secondary-fixed: '#ffdbcd'
  secondary-fixed-dim: '#ffb596'
  on-secondary-fixed: '#360f00'
  on-secondary-fixed-variant: '#7c2e00'
  tertiary-fixed: '#72fe88'
  tertiary-fixed-dim: '#53e16f'
  on-tertiary-fixed: '#002107'
  on-tertiary-fixed-variant: '#00531c'
  background: '#f8f9ff'
  on-background: '#161c25'
  surface-variant: '#dde3ef'
typography:
  headline-lg:
    fontFamily: Plus Jakarta Sans
    fontSize: 32px
    fontWeight: '700'
    lineHeight: 40px
  headline-lg-mobile:
    fontFamily: Plus Jakarta Sans
    fontSize: 26px
    fontWeight: '700'
    lineHeight: 34px
  headline-md:
    fontFamily: Plus Jakarta Sans
    fontSize: 22px
    fontWeight: '700'
    lineHeight: 30px
  title-lg:
    fontFamily: Plus Jakarta Sans
    fontSize: 18px
    fontWeight: '600'
    lineHeight: 26px
  title-md:
    fontFamily: Plus Jakarta Sans
    fontSize: 16px
    fontWeight: '600'
    lineHeight: 24px
  body-lg:
    fontFamily: Plus Jakarta Sans
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
  body-md:
    fontFamily: Plus Jakarta Sans
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 20px
  label-lg:
    fontFamily: Plus Jakarta Sans
    fontSize: 14px
    fontWeight: '600'
    lineHeight: 18px
  label-md:
    fontFamily: Plus Jakarta Sans
    fontSize: 12px
    fontWeight: '500'
    lineHeight: 16px
  label-sm:
    fontFamily: Plus Jakarta Sans
    fontSize: 11px
    fontWeight: '600'
    lineHeight: 14px
  numeral-display:
    fontFamily: Plus Jakarta Sans
    fontSize: 28px
    fontWeight: '800'
    lineHeight: 36px
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  gutter: 1rem
  margin: 1.25rem
  space-xs: 0.25rem
  space-sm: 0.5rem
  space-md: 1rem
  space-lg: 1.25rem
  space-xl: 2rem
---

## Brand & Style

The design system establishes a focused, calm, and dependable environment tailored for personal financial management and recurring expense tracking. The aesthetic balances Korean fintech precision with humanized simplicity, providing immediate financial clarity without clutter.

### Key Pillars
- **Radical Transparency:** Financial data is structured with zero friction. Complex billing schedules, cycle conversions, and price delta adjustments are surfaced through clear typographic hierarchy.
- **Controlled Reassurance:** High-stakes alerts (upcoming renewals, unexpected rate changes) avoid inducing panic. Warning cues use calibrated warmth rather than aggressive red flags.
- **Tactile Utility:** Micro-interactions mimic high-precision digital ledger instruments with snappy transitions, distinct press feedback, and tactile containment.

## Colors

The palette is engineered around clean readability, strict data division, and purposeful accent distribution.

### Color Tokens & System Roles
- **Base Canvas (`#FFFFFF`):** Pure white background dedicated to maintaining maximum contrast across high-density numerical lists.
- **Surface Elevation (`#F4F6F8`):** Off-white tinted gray for nested cards, ledger line items, and modular containers.
- **Subtle Stroke (`#E5E8EB`):** Boundary separation token that keeps stacked card components legible on identical or soft backgrounds.
- **Deep Indigo (`#3D4FD6`):** Primary system driver for primary actions, active bottom navigation states, total spending graphs, and high-priority filters.
- **Alert Amber-Orange (`#FA6400`):** Semantic warning token applied specifically to renewal count-downs (D-Day badges), price-surge indicators, and trial-end warnings.
- **Success Mint (`#34C759`):** Reserved for discount applied tags, successful auto-pay validations, and downward cost deltas.
- **Neutral Ladder:**
  - `Text/Primary` (`#191F28`): Core figures, currency symbols, and titles.
  - `Text/Secondary` (`#4E5968`): Meta captions, cycle intervals, and label anchors.
  - `Text/Disabled` (`#8B95A1`): Inactive placeholders, past cycles, and tertiary iconography.

## Typography

The type scale uses geometric grotesque figures paired with Korean glyph system fallbacks (Pretendard system font stack in production).

### Numerical Hierarchy
- **Primary Metric Displays:** Large currency amounts (`87,400원`) use `numeral-display` with standard proportional tabular figures (`tnum`) enabled to ensure aligning columns remain strictly identical in width.
- **Symbol Scale:** The Korean Won character (`원`) trailing standard numbers scales 2px down from the numerical root size with `fontWeight: 600` to anchor numeric hierarchy.
- **Section Headers:** Viewport-level totals utilize bold, tight leading to eliminate dead space in mobile hero regions.

## Layout & Spacing

The architecture operates on an 8pt baseline grid tailored for Android hand ergonomics.

### Screen Layout Principles
- **Outer Canvas Margins:** Strict `1.25rem` (20px) margins keep data cards inset safely away from physical hardware edges and gesture regions.
- **Component Gutters:** Intra-card gaps remain constant at `0.75rem` (12px) to `1rem` (16px), grouping monthly timeline items without excessive whitespace drift.
- **Safe Area Adaptations:** The bottom tab bar accounts for Android edge-to-edge transparent navigation handles by adding bottom inset padding of 24px beneath standard icon heights.
- **Card Content Rhythm:** Standard content card interior padding defaults to `1rem` (16px), maintaining uniform optical weight across service summary cards.

## Elevation & Depth

Visual hierarchy uses crisp boundary surfaces rather than dramatic shadows, matching high-utility Korean finance tools.

### Elevation Levels
- **Canvas Base (Level 0):** Pure white `#FFFFFF` default view surface.
- **Card Substrate (Level 1):** `#F4F6F8` fill with a subtle 1px border stroke using `#E5E8EB`. No drop shadow.
- **Floating Modals & Bottom Sheets (Level 2):** `#FFFFFF` fill with an extra-diffused ambient shadow: `box-shadow: 0 10px 30px -10px rgba(25, 31, 40, 0.08)`.
- **Primary Action Affordance:** CTA buttons in `#3D4FD6` remain flat without elevation gradients, switching to `#303FB3` upon direct active touch state.

## Shapes

The design system uses deliberate corner curvature tokens tailored to nested visual elements:

### Radii Hierarchy
- **Cards & Data Groups:** Set strictly to `16px` (`rounded-lg`) to create distinct, modern content grouping blocks.
- **Interactive Controls (Buttons, Inputs, Pill Chips):** Set to `12px` (`rounded-md`), pairing cleanly inside parent 16px cards without corner tension.
- **Service Logo Containers:** Set to `12px` rounded squares with an interior border overlay of `rgba(0, 0, 0, 0.04)` to prevent bright provider brandmarks from bleeding into the background.
- **Badge Pills:** Full circular caps (`9999px`) for contextual D-Day markers and status pills.

## Components

### Buttons
- **Primary Button:** Height 52px, background `#3D4FD6`, color `#FFFFFF`, border-radius 12px, font `title-md`. Touch-down state drops lightness by 10%.
- **Secondary / Ghost Button:** Height 44px, background `#F4F6F8`, color `#4E5968`, zero border, border-radius 12px.

### Chips & Badges
- **D-Day Chip:** Height 24px, padding 2px 8px, border-radius 9999px. Background `#FFF0E8`, typography `label-sm` with text color `#FA6400`.
- **Status Filter Chips:** Height 36px, padding 8px 14px, border-radius 12px. Inactive state: `#F4F6F8` text `#4E5968`. Active state: `#191F28` text `#FFFFFF`.

### Service Cards
- Padding 16px, border-radius 16px, background `#F4F6F8`, border 1px solid `#E5E8EB`.
- Left-aligned 44x44px rounded-square logo container, centered title and billing cycle subtitle, right-aligned bold amount (`14,900원`) with renewal pill indicator directly below.

### Inputs & Entry Fields
- Height 52px, surface `#FFFFFF`, border 1px solid `#E5E8EB`, border-radius 12px, padding horizontal 16px. Active focus switches outline to 1.5px solid `#3D4FD6`.

### Checkboxes & Radios
- 20x20px dimension. Unchecked: 1.5px border `#E5E8EB` with clear background. Checked: `#3D4FD6` fill with bold white micro-check.

### Bottom Navigation Bar
- 5 items: 홈 (Home), 목록 (List), 캘린더 (Calendar), 통계 (Analytics), 더보기 (More).
- Height 64px + safe area padding. Background `#FFFFFF`, top border 1px solid `#E5E8EB`. Active item uses `#3D4FD6` icon and label; inactive items use `#8B95A1`.