package com.flashlearn.app.ui.icons

import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import com.mikepenz.iconics.compose.Image as IconicsImage
import com.mikepenz.iconics.typeface.IIcon
import com.mikepenz.iconics.typeface.library.community.material.CommunityMaterial

fun mdiIcon(name: String): IIcon {
    val key = "cmd_" + name.replace('-', '_')
    return runCatching { CommunityMaterial.getIcon(key) }
        .getOrElse { CommunityMaterial.getIcon("cmd_help_circle_outline") }
}

@Composable
fun Icon(
    imageVector: IIcon,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current
) {
    IconicsImage(asset = imageVector, contentDescription = contentDescription, modifier = modifier, colorFilter = ColorFilter.tint(tint))
}
