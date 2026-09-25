package com.flashlearn.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import com.flashlearn.app.ui.theme.LocalFlashLearnThemeTokens

/**
 * FlashLearnScreen - Universal Screen Wrapper
 * 
 * تمام صفحات باید با این wrapper ساخته شوند.
 * این wrapper خودکار:
 * - MaterialTheme.colorScheme.background استفاده می‌کند
 * - MaterialTheme.shapes استفاده می‌کند
 * - MaterialTheme.typography استفاده می‌کند
 * - Tokens از LocalFlashLearnThemeTokens استفاده می‌کند
 * 
 * وقتی تم تغییر شود:
 * - تمام رنگ‌ها خودکار تغییر می‌کند
 * - تمام Corners خودکار تغییر می‌کند
 * - تمام Typography خودکار تغییر می‌کند
 * - تمام Spacing خودکار تغییر می‌کند
 * 
 * مثال:
 * @Composable
 * fun MyScreen() {
 *     FlashLearnScreen {
 *         // Content اینجا
 *     }
 * }
 */
@Composable
fun FlashLearnScreen(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
        shape = RectangleShape
    ) {
        content()
    }
}

/**
 * FlashLearnButton - Universal Button Component
 * 
 * تمام Buttons باید بر اساس این الگو ساخته شوند.
 * 
 * مثال:
 * Button(
 *     onClick = { },
 *     modifier = Modifier.height(tokens.controlHeight),
 *     shape = MaterialTheme.shapes.medium
 * ) { Text("Label") }
 */

/**
 * FlashLearnDivider - Universal Divider
 */
@Composable
fun FlashLearnDivider(
    modifier: Modifier = Modifier
) {
    val tokens = LocalFlashLearnThemeTokens.current
    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.outline)
            .padding(vertical = tokens.tinyGap)
    )
}

/**
 * FlashLearnIconButton - Universal Icon Button
 */

/**
 * FlashLearnTextField - Universal Text Field
 */

// ============================================================================
// USAGE RULES
// ============================================================================
//
// Rule 1: تمام Screens باید FlashLearnScreen() استفاده کنند
// ❌ Column(Modifier.fillMaxSize()) { ... }
// ✅ FlashLearnScreen { Column { ... } }
//
// Rule 2: تمام Cards باید FlashLearnCard() استفاده کنند
// ❌ Card(shape = RoundedCornerShape(20.dp)) { ... }
// ✅ FlashLearnCard { ... }
//
// Rule 3: تمام Colors باید MaterialTheme استفاده کنند
// ❌ Text(color = Color(0xFF...))
// ✅ Text(color = MaterialTheme.colorScheme.primary)
//
// Rule 4: تمام Shapes باید MaterialTheme استفاده کنند
// ❌ Button(shape = RoundedCornerShape(16.dp))
// ✅ Button(shape = MaterialTheme.shapes.medium)
//
// Rule 5: تمام Spacing باید Tokens استفاده کند
// ❌ Spacer(Modifier.height(12.dp))
// ✅ Spacer(Modifier.height(tokens.contentGap))
//
// Rule 6: تمام Typography باید MaterialTheme استفاده کند
// ❌ Text("Label", fontSize = 16.sp)
// ✅ Text("Label", style = MaterialTheme.typography.titleMedium)
//
// اگر این rules رعایت شود:
// ✅ Theme switching خودکار کار می‌کند
// ✅ تمام صفحات تغییر می‌کنند
// ✅ بدون دستی fixes

