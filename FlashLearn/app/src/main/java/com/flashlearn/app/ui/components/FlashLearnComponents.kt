package com.flashlearn.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
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
                    fontWeight = FontWeight.ExtraBold,
                    color = tokens.reviewText,
                    textAlign = TextAlign.Start
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
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            subtitle?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, color = tokens.onSurfaceVariant)
            }
        }
        trailing?.invoke() ?: Spacer(Modifier.width(tokens.iconLarge))
    }
}

/** Primary action button – reacts to theme personality (filled + stronger on Luxury) */
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
        shape = MaterialTheme.shapes.large,
        colors = ButtonDefaults.buttonColors(
            containerColor = tokens.primary,
            contentColor = tokens.onPrimary
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = if (tokens.preferFilledButtons) tokens.cardElevation else 0.dp
        ),
        content = content
    )
}

/** Secondary / outline button */
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
        shape = MaterialTheme.shapes.large,
        border = BorderStroke(tokens.borderThin, tokens.primary.copy(alpha = 0.7f)),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = tokens.primary),
        content = content
    )
}

/** Standard content card – border + elevation strength come from the active theme */
@Composable
fun FlashLearnCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val tokens = LocalFlashLearnThemeTokens.current
    val border = BorderStroke(
        tokens.borderThin,
        tokens.outlineColor.copy(alpha = tokens.cardBorderAlpha)
    )
    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = modifier,
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(containerColor = tokens.cardColor),
            elevation = CardDefaults.cardElevation(defaultElevation = tokens.cardElevation),
            border = border,
            content = content
        )
    } else {
        Card(
            modifier = modifier,
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(containerColor = tokens.cardColor),
            elevation = CardDefaults.cardElevation(defaultElevation = tokens.cardElevation),
            border = border,
            content = content
        )
    }
}

/** Stat number tile used on Home / Progress – fully theme driven */
@Composable
fun FlashLearnStatTile(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    val tokens = LocalFlashLearnThemeTokens.current
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = tokens.surfaceVariant.copy(alpha = 0.55f),
        border = BorderStroke(tokens.borderThin, tokens.outlineColor.copy(alpha = tokens.cardBorderAlpha * 0.8f))
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(vertical = tokens.contentPadding, horizontal = tokens.compactPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(tokens.tinyGap)
        ) {
            Text(
                value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = tokens.primary,
                textAlign = TextAlign.Center
            )
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                color = tokens.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

/** Accent icon tile (used in review cards, list items, etc.) */
@Composable
fun FlashLearnIconTile(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    contentDescription: String? = null
) {
    val tokens = LocalFlashLearnThemeTokens.current
    Surface(
        modifier = modifier.size(tokens.iconTileSize),
        shape = RoundedCornerShape(tokens.cornerSmall),
        color = tokens.primary.copy(alpha = tokens.accentSurfaceAlpha)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                icon,
                contentDescription = contentDescription,
                tint = tokens.primary,
                modifier = Modifier.size(tokens.iconLarge)
            )
        }
    }
}

/** Section title with consistent hierarchy */
@Composable
fun FlashLearnSectionTitle(
    text: String,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null
) {
    val tokens = LocalFlashLearnThemeTokens.current
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = tokens.onSurface,
            modifier = Modifier.weight(1f)
        )
        trailing?.invoke()
    }
}
