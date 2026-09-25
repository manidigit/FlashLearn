package com.flashlearn.app.ui.theme

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Semantic icon facade.
 *
 * Screens depend on meaning (Back, Settings, Search, ...), not on a concrete
 * Material icon family. The active theme chooses the family where both
 * variants exist. Icons without a meaningful family variant intentionally
 * remain direct/fixed assets and should not be forced through this facade.
 */
@androidx.compose.runtime.Immutable
data class FlashLearnIconSet(
    val style: IconStyle,
    val back: ImageVector,
    val add: ImageVector,
    val close: ImageVector,
    val search: ImageVector,
    val refresh: ImageVector,
    val settings: ImageVector,
    val home: ImageVector,
    val review: ImageVector,
    val library: ImageVector,
    val progress: ImageVector,
) {
    companion object {
        fun forStyle(style: IconStyle): FlashLearnIconSet {
            val filled = style == IconStyle.FILLED
            return FlashLearnIconSet(
                style = style,
                back = if (filled) Icons.AutoMirrored.Filled.ArrowBack else Icons.AutoMirrored.Outlined.ArrowBack,
                add = if (filled) Icons.Filled.Add else Icons.Outlined.Add,
                close = if (filled) Icons.Filled.Close else Icons.Outlined.Close,
                search = if (filled) Icons.Filled.Search else Icons.Outlined.Search,
                refresh = if (filled) Icons.Filled.Refresh else Icons.Outlined.Refresh,
                settings = if (filled) Icons.Filled.Settings else Icons.Outlined.Settings,
                home = if (filled) Icons.Filled.Home else Icons.Outlined.Home,
                review = if (filled) Icons.Filled.History else Icons.Outlined.History,
                library = if (filled) Icons.Filled.MenuBook else Icons.Outlined.MenuBook,
                progress = if (filled) Icons.Filled.BarChart else Icons.Outlined.BarChart,
            )
        }
    }
}
