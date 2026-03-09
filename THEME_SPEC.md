# Design System: Expense Tracker / Project Tracker
**Project ID:** 11233701278354758577

## 1. Visual Theme & Atmosphere
The design presents a clean, professional, and slightly utilitarian atmosphere with a focus on data clarity and structured forms. The use of soft, light-gray backgrounds contrasting against crisp, pure-white cards creates a modern, airy feel. The deep, muted slate-teal primary color grounds the visual weight, imparting a sense of reliability and enterprise-level sophistication suitable for an expense management tool.

## 2. Color Palette & Roles

*   **Deep Slate-Teal (Primary)** (`#2F3E46`): The core brand color. Used for active states, main headers, primary action buttons, filled progress bars, and hero elements.
*   **Light Gray Background** (`#F7F7F7`): The main application background. Provides subtle contrast against white components to create depth without heavy shadows.
*   **Dark Background** (`#171A1B`): Used as the primary background in dark mode.
*   **Pure White (Surface)** (`#FFFFFF`): Used for primary cards, form inputs, and main content areas to create elevation.
*   **Primary Text** (`Slate-900` / `~#0F172A`): Used for primary headers, standard body text, and input values.
*   **Secondary Text & Labels** (`Slate-500` / `~#64748B`): Used for form labels, subtext, empty states, and inactive icons.
*   **Borders & Dividers** (`Slate-200` / `~#E2E8F0`): Used for input borders, card outlines, and section dividers to provide structured layout separators.
*   **Success Green** (`Green-100` bg `~#DCFCE7` / `Green-700` text `~#15803D`): Used functionally for "Paid" or "Completed" status badges.
*   **Warning Amber** (`Amber-100` bg `~#FEF3C7` / `Amber-700` text `~#B45309`): Used functionally for "Pending" status badges.
*   **Action Blue** (`Blue-100` bg `~#DBEAFE` / `Blue-600` text `~#2563EB`): Used for specific categories (e.g., Travel) and "Reimbursed" statuses.

## 3. Typography Rules

*   **Font Family**: `Inter` (Sans-serif) is used throughout the entire application for maximum legibility.
*   **Headers & prominent Values**: Uses Bold weight (700) to anchor the visual hierarchy (e.g., "$12,400", "Project Tracker").
*   **Form Labels & Section Subheaders**: Uses Semibold weight (600), typically styled in all uppercase (`uppercase`) with expanded letter spacing (`tracking-wider`) and very small font size (`text-xs`). This gives a structured, highly technical, and tidy feel.
*   **Navigation & Secondary Actions**: Uses Medium weight (500) at a small size (`text-sm` or `text-xs`) to ensure readability without competing with main content.
*   **Body & Descriptions**: Standard Regular weight (400), often set at `text-sm` for a denser layout typical of dashboard applications.

## 4. Component Stylings

*   **Primary Buttons**: Generously rounded corners (0.75rem / `rounded-xl`), filled with the primary Deep Slate-Teal color, bold white text, and lacking any drop shadow, offering a flat but distinct aesthetic.
*   **Secondary/Action Buttons**: Subtly rounded corners (0.5rem / `rounded-lg`), pure white background with a light slate border (`border-slate-200`), primary colored text, and a very subtle drop shadow (`shadow-sm`) to indicate clickability.
*   **Cards/Containers**: Generously rounded corners (0.75rem / `rounded-xl`), pure white background, and distinct but light borders (`border-slate-100` or `border-slate-200`). They use flat design over actual layered shadows to differentiate from the background.
*   **Inputs/Forms**: Subtly rounded corners (0.5rem / `rounded-lg`) with light borders (`border-slate-200`). The focus state highlights the border and ring in the primary color (`focus:ring-primary focus:border-primary`).
*   **Badges/Tags**: Pill-shaped (fully rounded / `rounded-full`), utilizing a light tinted background with dark tinted text of the same hue family to indicate status visually.
*   **Interactive Zones (Uploads)**: Feature dashed borders (`border-dashed border-slate-200`) and a slightly tinted background (`bg-slate-50`) to indicate a dropzone or special interactive area.

## 5. Layout Principles

*   **Spacing Strategy**: Dense but clear hierarchical layout using consistent spacing scales (e.g., 1rem/16px gaps, 1.5rem/24px padding for main sections).
*   **Grid Alignments**: Strong reliance on 2-column grids (`grid-cols-2`) for desktop-width forms to maximize space efficiency without overwhelming the user.
*   **Flexbox Alignment**: Heavy use of flexbox for aligning icons symmetrically with text horizontally (`items-center gap-2` or `gap-4`).
*   **Elevation**: The main application view itself uses a noticeable diffused shadow (`shadow-xl`) to lift the entire app container from the background, while internal elements rely on borders and background contrasted layering rather than heavy shadows.
