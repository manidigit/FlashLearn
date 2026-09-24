package com.flashlearn.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.flashlearn.app.ui.theme.IconStyle
import com.flashlearn.app.ui.theme.LocalFlashLearnThemeTokens

@Composable
fun FlashLearnIcon(
    imageVector: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: androidx.compose.ui.graphics.Color = LocalFlashLearnThemeTokens.current.onSurface
) {
    Icon(imageVector, contentDescription, modifier = modifier, tint = tint)
}

@Composable
fun FlashLearnBackButton(
    onClick: () -> Unit,
    contentDescription: String = "بازگشت",
    enabled: Boolean = true
) {
    val tokens = LocalFlashLearnThemeTokens.current
    val icon = if (tokens.iconStyle == IconStyle.FILLED) {
        Icons.AutoMirrored.Filled.ArrowBack
    } else {
        Icons.AutoMirrored.Outlined.ArrowBack
    }
    IconButton(onClick = onClick, enabled = enabled) {
        FlashLearnIcon(icon, contentDescription, tint = tokens.onSurface)
    }
}

enum class FlashLearnScreenHeaderVariant { STANDARD, REVIEW }

@Composable
fun FlashLearnScreenHeader(
    title: String,
    onBack: (() -> Unit)? = null,
    subtitle: String? = null,
    trailing: (@Composable () -> Unit)? = null,
    variant: FlashLearnScreenHeaderVariant = FlashLearnScreenHeaderVariant.STANDARD
) {
    val tokens = LocalFlashLearnThemeTokens.current
    if (variant == FlashLearnScreenHeaderVariant.REVIEW) {
        Row(
            Modifier.fillMaxWidth().height(tokens.reviewHeaderHeight),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                Modifier.weight(1f),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    title,
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold,
                    color = tokens.reviewText,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Start
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(tokens.reviewTinyGap)
                ) {
                    Box(Modifier.width(tokens.reviewOrnamentLine).height(tokens.reviewOrnamentHeight).background(tokens.reviewAccent))
                    FlashLearnIcon(Icons.Outlined.AutoAwesome, null, tint = tokens.reviewAccent, modifier = Modifier.size(tokens.reviewOrnamentIcon))
                    Box(Modifier.width(tokens.reviewOrnamentLine).height(tokens.reviewOrnamentHeight).background(tokens.reviewAccent))
                }
            }
            Spacer(Modifier.width(tokens.reviewHeaderGap))
            Surface(
                modifier = Modifier.size(tokens.reviewBackButtonSize).clickable(enabled = onBack != null) { onBack?.invoke() },
                shape = CircleShape,
                color = tokens.reviewSurface,
                tonalElevation = tokens.reviewBackElevation,
                shadowElevation = tokens.reviewBackElevation
            ) {
                Box(contentAlignment = Alignment.Center) {
                    FlashLearnIcon(Icons.AutoMirrored.Outlined.ArrowBack, "بازگشت", tint = tokens.reviewAccent, modifier = Modifier.size(tokens.reviewBackIcon))
                }
            }
        }
        return
    }
    Row(
        Modifier.fillMaxWidth().height(tokens.headerHeight).padding(horizontal = tokens.screenPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onBack != null) {
            FlashLearnBackButton(onBack)
        } else {
            Spacer(Modifier.width(tokens.iconLarge))
        }
        Column(Modifier.weight(1f), horizontalAlignment = Alignment.Start) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            subtitle?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, color = tokens.onSurfaceVariant)
            }
        }
        trailing?.invoke() ?: Spacer(Modifier.width(tokens.iconLarge))
    }
}

@Composable
fun FlashLearnPrimaryButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit
) {
    val tokens = LocalFlashLearnThemeTokens.current
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(tokens.buttonHeight),
        shape = MaterialTheme.shapes.medium,
        content = content
    )
}

@Composable
fun FlashLearnSecondaryButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit
) {
    val tokens = LocalFlashLearnThemeTokens.current
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(tokens.buttonHeight),
        shape = MaterialTheme.shapes.medium,
        content = content
    )
}

@Composable
fun FlashLearnCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val tokens = LocalFlashLearnThemeTokens.current
    Card(
        onClick = onClick ?: {},
        enabled = onClick != null,
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = tokens.cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = tokens.cardElevation),
        content = content
    )
}


@Composable
fun FlashLearnIconTile(
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    val tokens = LocalFlashLearnThemeTokens.current
    Surface(
        modifier = modifier.size(tokens.iconTileSize),
        shape = RoundedCornerShape(tokens.smallCorner),
        color = tokens.primary.copy(alpha = tokens.accentSurfaceAlpha)
    ) {
        Box(contentAlignment = Alignment.Center) {
            FlashLearnIcon(icon, null, tint = tokens.primary, modifier = Modifier.size(tokens.iconMedium))
        }
    }
}

@Composable
fun FlashLearnStatTile(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    val tokens = LocalFlashLearnThemeTokens.current
    Surface(
        modifier = modifier.height(tokens.statCardHeight),
        shape = RoundedCornerShape(tokens.mediumCorner),
        color = tokens.surfaceVariant
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(tokens.contentPadding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = tokens.primary)
            Spacer(Modifier.height(tokens.tinyGap))
            Text(label, style = MaterialTheme.typography.bodySmall, color = tokens.onSurfaceVariant)
        }
    }
}

@Composable
fun FlashLearnSectionTitle(
    text: String,
    trailing: (@Composable () -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val tokens = LocalFlashLearnThemeTokens.current
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(text, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = tokens.onSurface)
        Spacer(Modifier.weight(1f))
        trailing?.invoke()
    }
}
