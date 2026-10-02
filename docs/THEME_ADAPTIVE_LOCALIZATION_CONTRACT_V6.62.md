# FlashLearn v6.62 — Theme / Adaptive / Localization Contract

## 1. Runtime visual ownership
The runtime path is:

`FlashLearnThemeSpec → ThemeDesign → FlashLearnThemeTokens → Compose screens`

Built-in ThemeSpec fields such as legacy corner/scale values remain persisted for backward-compatible custom JSON. Runtime rendering must read the corresponding ThemeDesign metrics.

## 2. Adaptive layout
Layout decisions are based on available window width, not a device model or a fixed phone target.

Theme-owned metrics:
- `adaptiveMediumBreakpoint`
- `adaptiveExpandedBreakpoint`

The Home statistics section reflows to a vertical/2-column compact layout when the available width is below the medium breakpoint.

## 3. Localization boundary
- Static user-facing UI copy: `res/values*/strings.xml`
- User-created vocabulary/content: Room/domain models
- Theme design values: ThemeDesign
- Technical MIME types, route identifiers and enum names: source code

The project must not move static UI copy into Room.

## 4. Dynamic color policy
Material dynamic color is intentionally disabled. Wallpaper-derived colors must not silently replace an explicitly selected FlashLearn theme.

## 5. Verification contract
A release is not considered verified until both authoritative CI jobs pass:
- Build + Unit Test
- Instrumentation + Upgrade Gate

## 6. Regression contract
The ThemeDesign contract test verifies that built-in themes keep their legacy compatibility fields aligned with runtime ThemeDesign metrics.
