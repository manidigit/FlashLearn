package com.flashlearn.app.ui.icons

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import io.github.timoptr.mdiicons.Mdi
import io.github.timoptr.mdiicons.rememberImageVector

/**
 * Single icon gateway for FlashLearn.
 *
 * All UI icons come from the Pictogrammers Material Design Icons catalog.
 * Directional icons opt into RTL mirroring automatically.
 */
@Composable
fun mdiIcon(name: String, autoMirror: Boolean = true): ImageVector {
    val icon = Mdi.fromMdiName(name) ?: Mdi.fromMdiName("help-circle-outline")!!
    return icon.rememberImageVector(autoMirror = autoMirror)
}
