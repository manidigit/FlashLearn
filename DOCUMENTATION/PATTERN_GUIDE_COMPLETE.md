# 🎯 COMPLETE PATTERN GUIDE - تمام الگوهای اصلاح Theme

---

## ✅ PATTERN 1: Screen بدون ColorScheme

**Screens:** HomeScreen, SettingsScreen, BackupScreen, BulkImportScreen, LibraryScreen, NeedsReviewScreen, AboutScreen

### ❌ BEFORE
```kotlin
@Composable
fun YourScreen() {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        // محتوا
    }
}
```

### ✅ AFTER
```kotlin
@Composable
fun YourScreen() {
    val tokens = LocalFlashLearnThemeTokens.current
    
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(tokens.screenPadding),
            verticalArrangement = Arrangement.spacedBy(tokens.contentGap)
        ) {
            // محتوا
        }
    }
}
```

### نکات
- `MaterialTheme.colorScheme.background` برای Background
- `tokens.screenPadding` = 20.dp
- `tokens.contentGap` = 12.dp

---

## ✅ PATTERN 2: Card بدون Shapes

**Screens:** LibraryScreen, LibraryDetailScreen, NeedsReviewScreen, AboutScreen, HelpScreen, AddWordScreen

### ❌ BEFORE
```kotlin
Card(Modifier.fillMaxWidth()) {
    Column(Modifier.padding(16.dp)) {
        // محتوا
    }
}
```

### ✅ AFTER
```kotlin
Card(
    modifier = Modifier.fillMaxWidth(),
    shape = MaterialTheme.shapes.medium,
    colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surface
    )
) {
    Column(
        modifier = Modifier.padding(tokens.contentPadding),
        verticalArrangement = Arrangement.spacedBy(tokens.contentGap)
    ) {
        // محتوا
    }
}
```

### نکات
- `MaterialTheme.shapes.medium` برای Corners
- `MaterialTheme.colorScheme.surface` برای Background
- `tokens.contentPadding` = 16.dp

---

## ✅ PATTERN 3: Hardcoded DPs

### Mapping Table

| Hardcoded | Token | Value |
|-----------|-------|-------|
| 4.dp | tinyGap | 4.dp |
| 6.dp | microGap | 6.dp |
| 8.dp | compactGap | 8.dp |
| 12.dp | contentGap | 12.dp |
| 14.dp | reviewCardPadding | 14.dp |
| 16.dp | screenPadding, contentPadding | 16.dp |
| 18.dp | screenPadding * 0.9 | 18.dp |
| 20.dp | screenPadding | 20.dp |
| 48.dp | iconTileSize, controlHeight | 48dp-52dp |
| 52.dp | controlHeight, buttonHeight | 52.dp |

### ❌ BEFORE
```kotlin
Spacer(Modifier.height(12.dp))
Spacer(Modifier.width(8.dp))
Button(modifier = Modifier.height(48.dp))
```

### ✅ AFTER
```kotlin
Spacer(Modifier.height(tokens.contentGap))
Spacer(Modifier.width(tokens.compactGap))
Button(modifier = Modifier.height(tokens.controlHeight), shape = MaterialTheme.shapes.medium)
```

---

## ✅ PATTERN 4: Button بدون Shape

**Screens:** LibraryDetailScreen, NeedsReviewScreen, AboutScreen

### ❌ BEFORE
```kotlin
Button(
    onClick = { },
    modifier = Modifier.weight(1f).height(48.dp)
) { Text("ذخیره") }
```

### ✅ AFTER
```kotlin
Button(
    onClick = { },
    modifier = Modifier.weight(1f).height(tokens.controlHeight),
    shape = MaterialTheme.shapes.medium
) { Text("ذخیره") }
```

### برای OutlinedButton
```kotlin
OutlinedButton(
    onClick = { },
    modifier = Modifier.weight(1f).height(tokens.controlHeight),
    shape = MaterialTheme.shapes.medium,
    colors = OutlinedButtonDefaults.outlinedButtonColors(
        containerColor = MaterialTheme.colorScheme.surface
    )
) { Text("انصراف") }
```

---

## ✅ PATTERN 5: Row with Spacing

### ❌ BEFORE
```kotlin
Row(
    Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(10.dp)
)
```

### ✅ AFTER
```kotlin
Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(tokens.contentGap)
)
```

---

## ✅ PATTERN 6: Text Field

### ❌ BEFORE
```kotlin
OutlinedTextField(
    value = state,
    onValueChange = { state = it },
    shape = RoundedCornerShape(16.dp)
)
```

### ✅ AFTER
```kotlin
OutlinedTextField(
    value = state,
    onValueChange = { state = it },
    shape = MaterialTheme.shapes.medium,
    colors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = MaterialTheme.colorScheme.primary,
        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
        focusedContainerColor = MaterialTheme.colorScheme.surface,
        unfocusedContainerColor = MaterialTheme.colorScheme.surface
    )
)
```

---

## ✅ PATTERN 7: Icon + Text Spacing

### ❌ BEFORE
```kotlin
Row {
    Icon(Icons.Outlined.Save, null)
    Spacer(Modifier.width(8.dp))
    Text("ذخیره")
}
```

### ✅ AFTER
```kotlin
Row(horizontalArrangement = Arrangement.spacedBy(tokens.compactGap)) {
    Icon(Icons.Outlined.Save, null)
    Text("ذخیره")
}
```

---

## ✅ PATTERN 8: Surface Wrapper (Full Screen)

**استفاده برای:** تمام Screens

### Template
```kotlin
@Composable
fun [ScreenName]Screen(...) {
    val tokens = LocalFlashLearnThemeTokens.current
    
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(tokens.screenPadding),
            verticalArrangement = Arrangement.spacedBy(tokens.contentGap)
        ) {
            // Header
            Text("عنوان", style = MaterialTheme.typography.headlineMedium)
            
            // Cards
            Card(
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(tokens.contentPadding),
                    verticalArrangement = Arrangement.spacedBy(tokens.contentGap)
                ) {
                    // Content
                }
            }
        }
    }
}
```

---

## 📋 IMPORTS REQUIRED

```kotlin
import androidx.compose.material3.*
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButtonDefaults
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.ui.graphics.RectangleShape
import com.flashlearn.app.ui.theme.LocalFlashLearnThemeTokens
```

---

## 🔍 Checklist برای هر Screen

- [ ] `val tokens = LocalFlashLearnThemeTokens.current` اضافه شد
- [ ] Root `Column` داخل `Surface(color = MaterialTheme.colorScheme.background)` است
- [ ] تمام `Cards` دارای `shape = MaterialTheme.shapes.medium` هستند
- [ ] تمام `Cards` دارای `colors = CardDefaults.cardColors(containerColor = ...)` هستند
- [ ] تمام `Buttons` دارای `shape = MaterialTheme.shapes.medium` هستند
- [ ] تمام Spacing از tokens استفاده می‌کند (`contentGap`, `compactGap`, etc)
- [ ] هیچ hardcoded `.dp` ندارد

---

## 🎯 Priority Order

### 🔴 CRITICAL (Fix First)
1. LibraryDetailScreen ✅
2. LibraryScreen
3. NeedsReviewScreen ✅
4. AboutScreen ✅

### 🟠 HIGH
5. CategorySelectionScreen
6. HelpScreen
7. HomeScreen

### 🟡 MEDIUM
8. AddWordScreen
9. SettingsScreen
10. BackupScreen
11. BulkImportScreen
12. ProgressScreen

---

## 💡 Quick Copy-Paste Blocks

### Screen Wrapper
```kotlin
Surface(
    modifier = Modifier.fillMaxSize(),
    color = MaterialTheme.colorScheme.background
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(tokens.screenPadding),
        verticalArrangement = Arrangement.spacedBy(tokens.contentGap)
    ) {
        // Content here
    }
}
```

### Card Block
```kotlin
Card(
    modifier = Modifier.fillMaxWidth(),
    shape = MaterialTheme.shapes.medium,
    colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surface
    )
) {
    Column(
        modifier = Modifier.padding(tokens.contentPadding),
        verticalArrangement = Arrangement.spacedBy(tokens.contentGap)
    ) {
        // Content here
    }
}
```

### Button Row
```kotlin
Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(tokens.compactGap)
) {
    Button(
        onClick = { },
        modifier = Modifier.weight(1f).height(tokens.controlHeight),
        shape = MaterialTheme.shapes.medium
    ) { Text("ذخیره") }
    
    OutlinedButton(
        onClick = { },
        modifier = Modifier.weight(1f).height(tokens.controlHeight),
        shape = MaterialTheme.shapes.medium
    ) { Text("انصراف") }
}
```

