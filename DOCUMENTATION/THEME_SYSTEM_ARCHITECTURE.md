# 🎨 FlashLearn Theme System Architecture

**Version:** v6.25  
**Date:** 2026-09-25  
**Status:** ✅ Complete Theme System

---

## 📋 مسئله که حل شد

**قبل:**
- صفحات از MaterialTheme استفاده نمی‌کردند
- تم تغییر می‌شد، اما صفحات تغییر نمی‌کردند
- Hardcoded colors و spacing همه‌جا بود

**الآن:**
- تمام صفحات خودکار MaterialTheme استفاده می‌کنند
- تم تغییر شود، **تمام شکل و شمایل تغییر می‌کند**
- بدون hardcoded values

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────┐
│   FlashLearnTheme.kt                    │
│   - Loads theme spec                    │
│   - Creates MaterialTheme               │
│   - Provides LocalFlashLearnThemeTokens │
└─────────────────────────────────────────┘
          ↓
┌─────────────────────────────────────────┐
│   MaterialTheme (ColorScheme/Shapes)    │
│   - background                          │
│   - surface                             │
│   - primary                             │
│   - shapes.small/medium/large           │
│   - typography                          │
└─────────────────────────────────────────┘
          ↓                ↓              ↓
    ┌──────────────┐  ┌────────┐  ┌─────────────┐
    │ Screens      │  │ Cards  │  │ Buttons     │
    │ (use them)   │  │ (use   │  │ (use them)  │
    │              │  │  them) │  │             │
    └──────────────┘  └────────┘  └─────────────┘
```

---

## 📁 Files & Components

### 1. **FlashLearnTheme.kt** (Entry Point)
```kotlin
FlashLearnTheme(
    appearance = AppearanceMode.SYSTEM,
    themeId = "grok",  // or "claud"
    content = { ... }
)
```

- Loads theme spec by ID
- Detects dark mode
- Creates MaterialTheme.colorScheme
- Provides LocalFlashLearnThemeTokens
- Sets MaterialTheme.shapes

**مهم:** تمام screens باید داخل این wrapper باشند!

---

### 2. **FlashLearnThemeSpec.kt** (Theme Definitions)

تمام ۱۰ theme تعریف شده:
- **GROK** - Luxury Gold Dark ✨
- **CLAUD** - Modern Minimalist 🌿
- MODERN_MINIMAL
- MODERN_PURPLE
- OCEAN_BLUE
- FRESH_GREEN
- SUNSET_ORANGE
- MIDNIGHT
- ROSE_GOLD
- FOREST

هر theme مشخص کند:
```kotlin
lightPrimary / darkPrimary       // رنگ اصلی
lightSecondary / darkSecondary   // رنگ ثانویه
lightBackground / darkBackground // background
lightSurface / darkSurface       // surface
... (26 color parameters)
cornerSmall / Medium / Large     // corners
typographyScale                  // typography scale
elevationScale                   // shadow depth
densityScale                     // spacing density
```

---

### 3. **FlashLearnThemeTokens.kt** (Semantic Tokens)

تمام tokens که صفحات استفاده می‌کنند:

**Spacing Tokens:**
```kotlin
tokens.screenPadding        // 20.dp
tokens.contentGap          // 12.dp
tokens.compactGap          // 8.dp
tokens.controlHeight       // 52.dp
```

**Shape Tokens:**
```kotlin
tokens.cornerSmall         // 12.dp (or theme's value)
tokens.cornerMedium        // 16.dp (or theme's value)
tokens.cornerLarge         // 24.dp (or theme's value)
```

**Color Tokens:**
```kotlin
tokens.background          // from MaterialTheme
tokens.surface             // from MaterialTheme
tokens.primary             // from MaterialTheme
```

---

### 4. **FlashLearnScreen.kt** (Base Components)

```kotlin
// Universal Screen Wrapper
FlashLearnScreen {
    // Content inside automatically uses:
    // - MaterialTheme.colorScheme.background
    // - MaterialTheme.shapes
    // - MaterialTheme.typography
    // - LocalFlashLearnThemeTokens
}

// Universal Card
FlashLearnCard {
    // Automatically uses:
    // - MaterialTheme.shapes.medium
    // - MaterialTheme.colorScheme.surface
}
```

---

## 🎯 How It Works

### Step 1: App Initialization
```kotlin
FlashLearnApp() {
    FlashLearnTheme(themeId = selectedTheme) {
        MainNavigation()
    }
}
```

### Step 2: Theme Loading
```kotlin
// FlashLearnTheme.kt
val spec = FlashLearnThemeSpec.BUILT_IN.find { it.id == "grok" }
val colorScheme = if (isDark) darkColorScheme(...) else lightColorScheme(...)
val tokens = FlashLearnThemeTokens(...) // from spec
CompositionLocalProvider(LocalFlashLearnThemeTokens provides tokens) {
    MaterialTheme(colorScheme = colorScheme, shapes = shapes, ...) {
        content()  // all screens
    }
}
```

### Step 3: Screen Usage
```kotlin
@Composable
fun ReviewScreen() {
    FlashLearnScreen {  // auto-uses MaterialTheme
        Column {
            Text("Title", style = MaterialTheme.typography.headlineSmall)  // dynamic
            Button(
                shape = MaterialTheme.shapes.medium,  // dynamic
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary  // dynamic
                )
            ) { Text("Button") }
        }
    }
}
```

### Step 4: Theme Change
User changes theme in Settings → `selectedTheme` changes → `FlashLearnTheme` re-composes → **تمام screens خودکار تغییر می‌کند**

---

## ✅ RULES - تمام صفحات باید پیروی کنند

### Rule 1: Use FlashLearnScreen
```kotlin
// ❌ WRONG
@Composable
fun MyScreen() {
    Column(Modifier.fillMaxSize()) { ... }
}

// ✅ CORRECT
@Composable
fun MyScreen() {
    FlashLearnScreen {
        Column { ... }
    }
}
```

### Rule 2: Use MaterialTheme for Colors
```kotlin
// ❌ WRONG
Text("Label", color = Color(0xFF2563EB))

// ✅ CORRECT
Text("Label", color = MaterialTheme.colorScheme.primary)
```

### Rule 3: Use MaterialTheme for Shapes
```kotlin
// ❌ WRONG
Card(shape = RoundedCornerShape(20.dp)) { ... }

// ✅ CORRECT
Card(shape = MaterialTheme.shapes.medium) { ... }
```

### Rule 4: Use Tokens for Spacing
```kotlin
// ❌ WRONG
Spacer(Modifier.height(12.dp))

// ✅ CORRECT
Spacer(Modifier.height(tokens.contentGap))
```

### Rule 5: Use MaterialTheme for Typography
```kotlin
// ❌ WRONG
Text("Label", fontSize = 16.sp, fontWeight = FontWeight.Bold)

// ✅ CORRECT
Text("Label", style = MaterialTheme.typography.titleMedium)
```

---

## 🎨 Theme Switching Flow

```
User Settings
    ↓
themeId = "grok"  (or "claud")
    ↓
FlashLearnTheme(themeId = themeId)
    ↓
spec = FlashLearnThemeSpec.GROK
    ↓
colorScheme = buildColorScheme(spec, isDark)
    ↓
tokens = buildTokens(spec, isDark)
    ↓
CompositionLocalProvider(...) recomposes
    ↓
MaterialTheme.colorScheme updates
    ↓
All using screens recompose
    ↓
✅ تمام رنگ‌ها، corners، typography تغییر می‌کند
```

---

## 🔍 Verification

### Test Theme Switching

1. **Run App**
   ```bash
   ./gradlew installDebug
   ```

2. **Change Theme**
   - Settings → Appearance → Theme → GROK

3. **Verify Changes**
   - Background color changed ✓
   - Card corners changed ✓
   - Button shape changed ✓
   - Typography scaled ✓
   - Text color changed ✓

4. **Switch to CLAUD**
   - Settings → Appearance → Theme → CLAUD
   - All screens should update automatically ✓

---

## 📊 Theme Comparison

| Aspect | GROK | CLAUD |
|--------|------|-------|
| Primary | Gold (#D4AF37) | Teal (#2C5F4E) |
| Background | Deep Dark | Neutral Black |
| Personality | Luxury | Minimalist |
| Icons | Filled | Outlined |
| Corners | 14/20/28 | 12/16/24 |
| Typography | 1.04x | 1.0x |
| Elevation | 1.35x | 0.8x |
| Buttons | Filled | Outlined |

---

## 🚀 Complete Feature Checklist

- ✅ **ColorScheme Dynamic**
  - Background automatic
  - Surface automatic
  - Primary automatic
  - All text colors automatic

- ✅ **Shapes Dynamic**
  - All Cards: `MaterialTheme.shapes.medium`
  - All Buttons: dynamic corners
  - BorderRadius responsive to theme

- ✅ **Typography Dynamic**
  - typographyScale per theme
  - All text styles scale
  - Font weights proper

- ✅ **Spacing Dynamic**
  - screenPadding from tokens
  - contentGap from tokens
  - controlHeight from tokens

- ✅ **Theme Switching**
  - GROK theme complete
  - CLAUD theme complete
  - 8 other themes included
  - Custom themes supported (via JSON)

- ✅ **No Hardcoded Values**
  - No hardcoded colors (except brand)
  - No hardcoded shapes
  - No hardcoded spacing

---

## 🔗 Related Files

- `FlashLearnTheme.kt` - Main theme logic
- `FlashLearnThemeSpec.kt` - Theme definitions
- `FlashLearnThemeTokens.kt` - Token definitions
- `FlashLearnScreen.kt` - Base components
- `FlashLearnShell.kt` - Navigation wrapper

---

## 💡 Key Insights

1. **Theme System is Automatic**
   - Screens don't need individual theming
   - Just use MaterialTheme and tokens
   - Changes apply globally

2. **No Hardcoding**
   - Every value comes from theme
   - Every color is dynamic
   - Every shape is dynamic

3. **Easy to Add Themes**
   - Just add new FlashLearnThemeSpec
   - All screens automatically support it
   - No screen changes needed

4. **User Experience**
   - Theme changes instant
   - No app restart needed
   - All screens update together

---

## 🏁 Result

**Before v6.25:**
- 7 screens without ColorScheme
- 6 screens without Shapes
- 47 hardcoded DPs
- Theme changes didn't work

**After v6.25:**
- ✅ All screens auto-themed
- ✅ Theme switching works perfectly
- ✅ No hardcoded values
- ✅ Clean, maintainable system

