

# RTL/LTR + Theme Root-Cause Verification Addendum

> **MANDATORY AUDIT ADDENDUM — NON-OPTIONAL**
>
> This section is an obligatory part of the FlashLearn Audit specification. It must be executed as part of every relevant audit and must not be skipped, reduced to a screen-only visual check, or treated as optional polish.
>
> ## 1. CompositionLocalProvider / LocalLayoutDirection
>
> Find every occurrence of:
>
> `CompositionLocalProvider(LocalLayoutDirection provides ...)`
>
> and:
>
> `LocalLayoutDirection.current`
>
> For each occurrence record:
>
> - File
> - Screen
> - Component
> - Parent Direction
> - Local Direction
> - Hard-coded or Dynamic
> - Reason for use
> - Whether the override is required
>
> No Screen may override its parent LayoutDirection without a documented valid reason.
>
> Explicitly audit:
>
> - `LocalLayoutDirection provides LayoutDirection.Rtl`
> - `LocalLayoutDirection provides LayoutDirection.Ltr`
>
> Hard-coded values must be evaluated for removal when inheritance from the parent direction is the correct behavior.
>
> ## 2. Application Direction ↔ Compose Direction
>
> Verify every mapping between application direction and Compose direction.
>
> Required mapping:
>
> - Application RTL → Compose `LayoutDirection.Rtl`
> - Application LTR → Compose `LayoutDirection.Ltr`
>
> Any inverse mapping such as:
>
> ```kotlin
> if (isRtl) LayoutDirection.Ltr else LayoutDirection.Rtl
> ```
>
> is a **Potential Root Cause** and must be explicitly investigated.
>
> ## 3. Full Direction Trace
>
> Trace the actual value through the complete path:
>
> ```text
> Settings
> ↓
> Saved Preference
> ↓
> AppLayoutDirection
> ↓
> AppViewModel
> ↓
> AppUiState
> ↓
> MainActivity
> ↓
> CompositionLocalProvider
> ↓
> LocalLayoutDirection
> ↓
> Navigation
> ↓
> Screen
> ↓
> Nested Components
> ```
>
> Do not stop at MainActivity. A correct global value can still be overridden by Navigation, a Screen, or a nested Component.
>
> ## 4. Logical vs Physical Alignment
>
> Audit semantically:
>
> - `Alignment.Start`
> - `Alignment.End`
> - `Arrangement.Start`
> - `Arrangement.End`
> - `TextAlign.Start`
> - `TextAlign.End`
>
> Start/End are logical directions in Compose. They must not be replaced merely because RTL is enabled.
>
> Determine whether the intended semantics are Leading, Trailing, Start, End, Left, or Right before changing code.
>
> ## 5. Physical Alignment
>
> Explicitly inspect:
>
> - `Alignment.Left`
> - `Alignment.Right`
> - `TextAlign.Left`
> - `TextAlign.Right`
> - `padding(left = ...)`
> - `padding(right = ...)`
> - `offset(x = ...)`
> - `absoluteOffset(...)`
>
> Determine whether each use is intentionally physical or should be logical. Do not mechanically replace all occurrences.
>
> ## 6. Padding / Margin
>
> Audit left/right padding and their Compose equivalents.
>
> When the intended meaning is semantic leading/trailing spacing, evaluate whether `start`/`end` is required.
>
> Preserve physical positioning when it is intentional.
>
> ## 7. Directional Icons
>
> Find and classify all uses of:
>
> - ArrowBack
> - ArrowForward
> - ChevronLeft
> - ChevronRight
> - NavigateBefore
> - NavigateNext
> - Undo
> - Redo
> - Reply
> - Send
>
> For each determine:
>
> - Directional
> - Mirrored
> - Non-directional
> - Physical
>
> Only icons whose semantic meaning changes with layout direction may be converted to AutoMirrored.
>
> For navigation-back semantics, explicitly evaluate:
>
> `Icons.AutoMirrored.Outlined.ArrowBack`
>
> Do not convert every Left/Right icon indiscriminately.
>
> ## 8. Theme × Direction Matrix
>
> RTL/LTR must be audited together with Theme.
>
> Every target Screen must be verified in:
>
> - RTL + Light
> - RTL + Dark
> - LTR + Light
> - LTR + Dark
>
> Check:
>
> Layout Direction, Text Alignment, Component Alignment, Icon Direction, Padding, Spacing, Background, Surface, Text Color, Icon Color, Button Color, Border, Divider, Selection, Focus, Error, Disabled.
>
> ## 9. Theme Sources
>
> Audit every use of:
>
> - `MaterialTheme.colorScheme`
> - `MaterialTheme.typography`
> - `MaterialTheme.shapes`
>
> Also find:
>
> - `Color(...)`
> - `background(...)`
> - `contentColorFor(...)`
> - `TextStyle(...)`
>
> Determine whether hard-coded values conform to the project's Theme system.
>
> ## 10. Theme Overrides
>
> Find Screen/Component-level:
>
> - `MaterialTheme(...)`
> - `CompositionLocalProvider(...)`
>
> For each determine:
>
> - Is the override necessary?
> - Does it change Direction?
> - Does it change ColorScheme?
> - Does it change Typography?
> - Does it create inconsistent behavior with other Screens?
>
> ## 11. Mandatory Target Screens
>
> At minimum audit these Screens and all nested Components:
>
> - Add Word / افزودن واژه
> - Bulk Import / لغات گروهی
> - Backup Restore / ریستور بکاپ
> - Library / واژگان
> - Statistics / آمار
> - Review / مرور کلمات
>
> ## 12. Home and Review Regression Reference
>
> Re-verify previous Home and Review fixes.
>
> In particular, investigate:
>
> `CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl)`
>
> and:
>
> `CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr)`
>
> Neither may remain without a valid reason.
>
> Determine whether previous fixes corrected the shared root cause or only visually patched one Screen.
>
> ## 13. Direction Mapping Regression Test
>
> Add or verify an explicit test:
>
> - Application RTL → Compose RTL
> - Application LTR → Compose LTR
>
> Add regression coverage preventing:
>
> - RTL → LTR
> - LTR → RTL
>
> ## 14. Real UI Verification
>
> Unit tests alone are insufficient.
>
> Target Screens must be UI-tested in:
>
> - RTL + Light
> - RTL + Dark
> - LTR + Light
> - LTR + Dark
>
> Give special attention to:
>
> TopAppBar, Navigation, Text, Buttons, Cards, Lists, Forms, Dialogs, BottomSheets, Icons, Charts, Progress, Empty State, Error State, Loading State.
>
> ## 15. Root-Cause Priority
>
> Audit and fix in this order:
>
> **P0 — Global Direction**
>
> AppLayoutDirection, MainActivity, CompositionLocalProvider, LocalLayoutDirection, Navigation
>
> **P1 — Shared Components**
>
> Common Cards, Buttons, TopBars, Dialogs, TextFields, Lists, Navigation Components
>
> **P2 — Screen-specific**
>
> Add Word, Bulk Import, Backup Restore, Library, Statistics, Review
>
> **P3 — Visual polish**
>
> Spacing, Typography, minor padding, icon size, visual alignment
>
> Fix P0/P1 before creating Screen-specific patches. If a shared root cause exists, implement the shared fix rather than duplicating patches per Screen.
>
> ## 16. Mandatory Issue Record
>
> Every discovered issue must record:
>
> - Screen
> - Component
> - File
> - Line
> - Current Behavior
> - Expected Behavior
> - RTL Behavior
> - LTR Behavior
> - Light Theme
> - Dark Theme
> - Root Cause
> - Why previous fix did not solve it
> - Correct Fix
> - Regression Risk
> - Test Added
>
> ## 17. Root-Cause Neutrality
>
> The Audit must never assume that all problems are caused by RTL or Theme.
>
> Potential root causes include:
>
> - Wrong LayoutDirection
> - Hard-coded Direction
> - Wrong Alignment
> - Physical positioning
> - Wrong Padding
> - Wrong Icon
> - Wrong Theme token
> - Wrong Component
> - Wrong Navigation
> - Wrong State
> - Wrong Preview
> - Wrong Resource
> - Wrong Typography
> - Screen-specific override
>
> Find the root cause first, then fix it.
>
> ## 18. Final Completion Gate
>
> A Screen is not Correct merely because it compiles or Unit Tests are green.
>
> Completion requires all of:
>
> RTL correct
> + LTR correct
> + Light Theme correct
> + Dark Theme correct
> + Logical Alignment correct
> + Physical Alignment correct
> + Directional Icons correct
> + Padding/Spacing correct
> + Text/BiDi correct
> + Navigation correct
> + Forms correct
> + Dialogs correct
> + Shared Components correct
> + Regression Test
> + Build GREEN
> + Unit Test GREEN
> + UI/Instrumentation Test GREEN
> + CI GREEN
>
> **Until every applicable item above is satisfied, the Audit stage must not be marked Complete.**
