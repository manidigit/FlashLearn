package com.flashlearn.app.ui.theme

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Upload
import androidx.compose.material.icons.outlined.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Semantic action-icon facade.
 *
 * Screens select meaning rather than a Material icon family directly.
 * Spark uses the filled branch, while the other themes can keep outlined
 * actions without changing screen code.
 */
enum class FlashLearnAction {
    Back, Forward, Add, Close, Clear, Search, Refresh, Check,
    Favorite, Save, Upload, Volume, Home, Settings
}

@Composable
fun FlashLearnActionIcon(
    action: FlashLearnAction,
    contentDescription: String?,
    modifier: Modifier = Modifier
) {
    val filled = LocalFlashLearnThemeTokens.current.iconStyle == IconStyle.FILLED
    Icon(
        imageVector = action.icon(filled),
        contentDescription = contentDescription,
        modifier = modifier
    )
}

private fun FlashLearnAction.icon(filled: Boolean): ImageVector = when (this) {
    FlashLearnAction.Back -> if (filled) Icons.AutoMirrored.Filled.ArrowBack else Icons.AutoMirrored.Outlined.ArrowBack
    FlashLearnAction.Forward -> if (filled) Icons.Filled.ArrowForward else Icons.Outlined.ArrowForward
    FlashLearnAction.Add -> if (filled) Icons.Filled.Add else Icons.Outlined.Add
    FlashLearnAction.Close -> if (filled) Icons.Filled.Close else Icons.Outlined.Close
    FlashLearnAction.Clear -> if (filled) Icons.Filled.Clear else Icons.Outlined.Clear
    FlashLearnAction.Search -> if (filled) Icons.Filled.Search else Icons.Outlined.Search
    FlashLearnAction.Refresh -> if (filled) Icons.Filled.Refresh else Icons.Outlined.Refresh
    FlashLearnAction.Check -> if (filled) Icons.Filled.Check else Icons.Outlined.Check
    FlashLearnAction.Favorite -> if (filled) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder
    FlashLearnAction.Save -> if (filled) Icons.Filled.Save else Icons.Outlined.Save
    FlashLearnAction.Upload -> if (filled) Icons.Filled.Upload else Icons.Outlined.Upload
    FlashLearnAction.Volume -> if (filled) Icons.Filled.VolumeUp else Icons.Outlined.VolumeUp
    FlashLearnAction.Home -> if (filled) Icons.Filled.Home else Icons.Outlined.Home
    FlashLearnAction.Settings -> if (filled) Icons.Filled.Settings else Icons.Outlined.Settings
}
