package com.flashlearn.app.ui.library

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.flashlearn.app.ui.LanguagePair
import com.flashlearn.app.ui.theme.LocalFlashLearnThemeTokens
import com.flashlearn.domain.model.VocabularyDifficulty
import java.util.UUID

/**
 * LibraryScreen - نسخه refactored
 * 
 * تغییرات:
 * - Wrapped with Surface + MaterialTheme.colorScheme.background
 * - Replaced hardcoded DPs with tokens
 * - Added Material shapes to Cards
 * - Proper Color use from MaterialTheme
 */
@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel,
    languagePair: LanguagePair = LanguagePair(),
    onBack: () -> Unit = {},
    onOpen: (UUID) -> Unit = {},
    onAddWord: () -> Unit = {},
    onBulkImport: () -> Unit = {}
) {
    val tokens = LocalFlashLearnThemeTokens.current
    val state by viewModel.state.collectAsState()
    var duplicateMessage by remember { mutableStateOf<String?>(null) }
    
    LaunchedEffect(languagePair) { viewModel.setLanguagePair(languagePair) }
    LaunchedEffect(Unit) { viewModel.refresh() }
    
    // اصلاح اساسی: Wrap تمام چیز با Surface
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    horizontal = tokens.screenPadding,
                    vertical = tokens.screenVerticalPadding
                ),
            verticalArrangement = Arrangement.spacedBy(tokens.contentGap)
        ) {
            // Header
            LibraryHeader(tokens = tokens, onBack = onBack)
            
            // Search Field
            LibrarySearchField(
                query = state.query,
                onQueryChange = viewModel::onQueryChange,
                onRefresh = viewModel::refresh,
                isLoading = state.isLoading,
                tokens = tokens
            )
            
            Spacer(Modifier.height(tokens.sectionGap))
            
            // Stats Cards
            LibraryStatsRow(state = state, tokens = tokens)
            
            Spacer(Modifier.height(tokens.sectionGap))
            
            // Filters Section
            LibraryFiltersSection(
                state = state,
                viewModel = viewModel,
                tokens = tokens,
                duplicateMessage = duplicateMessage,
                onDuplicateMessageChange = { duplicateMessage = it }
            )
            
            // Items List
            when {
                state.isLoading -> Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
                
                state.error != null -> Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "خطا: ${state.error}",
                        color = MaterialTheme.colorScheme.error
                    )
                }
                
                state.items.isEmpty() -> Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("واژه‌ای پیدا نشد.")
                }
                
                else -> LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(tokens.itemGap),
                    contentPadding = PaddingValues(vertical = tokens.tinyGap)
                ) {
                    items(state.items, key = { it.concept.id }) { item ->
                        VocabularyCard(item, languagePair, tokens) {
                            onOpen(item.concept.id)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LibraryHeader(tokens: com.flashlearn.app.ui.theme.FlashLearnThemeTokens, onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(tokens.libraryHeaderHeight)
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            Icon(
                Icons.AutoMirrored.Outlined.ArrowBack,
                "بازگشت",
                tint = MaterialTheme.colorScheme.primary
            )
        }
        
        Text(
            "واژگان",
            modifier = Modifier.align(Alignment.Center),
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
        )
    }
}

@Composable
private fun LibrarySearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    onRefresh: () -> Unit,
    isLoading: Boolean,
    tokens: com.flashlearn.app.ui.theme.FlashLearnThemeTokens
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .height(tokens.librarySearchHeight),
        placeholder = {
            Text(
                "جستجو در واژگان...",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth()
            )
        },
        trailingIcon = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Outlined.Search,
                    "جستجو",
                    tint = MaterialTheme.colorScheme.primary
                )
                IconButton(onClick = onRefresh, enabled = !isLoading) {
                    Icon(
                        Icons.Outlined.Refresh,
                        "رفرش واژگان",
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        },
        singleLine = true,
        shape = MaterialTheme.shapes.medium,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@Composable
private fun LibraryStatsRow(
    state: LibraryViewModel.LibraryState,
    tokens: com.flashlearn.app.ui.theme.FlashLearnThemeTokens
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(tokens.itemGap)
    ) {
        LibraryStatCard(
            title = "جدید",
            value = state.newCount,
            color = MaterialTheme.colorScheme.error,
            icon = Icons.Outlined.Add,
            modifier = Modifier.weight(1f),
            tokens = tokens
        )
        
        LibraryStatCard(
            title = "در حال یادگیری",
            value = state.learningCount,
            color = MaterialTheme.colorScheme.error,
            icon = Icons.Outlined.History,
            modifier = Modifier.weight(1f),
            tokens = tokens
        )
        
        LibraryStatCard(
            title = "یادگرفته",
            value = state.learnedCount,
            color = MaterialTheme.colorScheme.error,
            icon = Icons.Outlined.CheckCircle,
            modifier = Modifier.weight(1f),
            tokens = tokens
        )
        
        LibraryStatCard(
            title = "کل واژگان",
            value = state.totalCount,
            color = MaterialTheme.colorScheme.primary,
            icon = Icons.Outlined.Book,
            modifier = Modifier.weight(1f),
            tokens = tokens
        )
    }
}

@Composable
private fun LibraryStatCard(
    title: String,
    value: Int,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier,
    tokens: com.flashlearn.app.ui.theme.FlashLearnThemeTokens
) {
    Card(
        modifier = modifier.height(tokens.libraryStatHeight),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.10f)
        ),
        border = BorderStroke(tokens.borderThin, color.copy(alpha = 0.14f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    vertical = tokens.compactPadding,
                    horizontal = tokens.tinyGap
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                icon,
                title,
                tint = color,
                modifier = Modifier.size(tokens.libraryStatIconSize)
            )
            Spacer(Modifier.height(tokens.tinyGap))
            Text(
                title,
                color = color,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center
            )
            Text(
                toFaDigits(value),
                color = color,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}

@Composable
private fun LibraryFiltersSection(
    state: LibraryViewModel.LibraryState,
    viewModel: LibraryViewModel,
    tokens: com.flashlearn.app.ui.theme.FlashLearnThemeTokens,
    duplicateMessage: String?,
    onDuplicateMessageChange: (String?) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End
    ) {
        Text(
            "فیلترها",
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
        )
        Spacer(Modifier.width(tokens.compactGap))
        Icon(
            Icons.Outlined.FilterList,
            "فیلترها",
            tint = MaterialTheme.colorScheme.primary
        )
    }
    
    Spacer(Modifier.height(tokens.contentGap))
    
    // Category Filter Card
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(tokens.borderThin, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(tokens.contentPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(tokens.contentGap)
        ) {
            Surface(
                modifier = Modifier.size(tokens.libraryIconTileSize),
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Outlined.Folder,
                        "دسته‌بندی‌ها",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(tokens.iconLarge)
                    )
                }
            }
            
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                Text(
                    "دسته‌بندی‌ها",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    "همه دسته‌ها • ${toFaDigits(state.categories.size)} دسته انتخاب شده",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            Icon(
                Icons.AutoMirrored.Outlined.ArrowBack,
                "انتخاب دسته",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
    
    Spacer(Modifier.height(tokens.tinyGap))
    
    // Category Chips
    if (state.categories.isNotEmpty()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(tokens.microGap)
        ) {
            FilterChip(
                selected = state.selectedCategoryId == null,
                onClick = { viewModel.onCategoryChange(null) },
                label = { Text("همه") }
            )
            
            state.categories.forEach { category ->
                FilterChip(
                    selected = state.selectedCategoryId == category.id,
                    onClick = { viewModel.onCategoryChange(category.id) },
                    label = { Text(category.name) }
                )
            }
        }
        Spacer(Modifier.height(tokens.tinyGap))
    }
    
    // Status and Duplicate Filters
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(tokens.microGap)
    ) {
        FilterChip(
            selected = !state.favoritesOnly,
            onClick = { viewModel.onFavoritesChange(false) },
            label = { Text("همه") }
        )
        FilterChip(
            selected = state.favoritesOnly,
            onClick = { viewModel.onFavoritesChange(true) },
            label = { Text("موردعلاقه‌ها") }
        )
        FilterChip(
            selected = false,
            onClick = {
                viewModel.removeExactDuplicates { count ->
                    onDuplicateMessageChange(
                        if (count == 0) "تکراری پیدا نشد."
                        else "${toFaDigits(count)} واژه تکراری Merge شد."
                    )
                }
            },
            enabled = !state.isDuplicateCleanupBusy,
            label = { Text(if (state.isDuplicateCleanupBusy) "در حال Merge..." else "تکراری‌ها") }
        )
    }
    
    Spacer(Modifier.height(tokens.compactGap))
    
    duplicateMessage?.let {
        Text(
            it,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.End,
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun VocabularyCard(
    item: LibraryItem,
    languagePair: LanguagePair,
    tokens: com.flashlearn.app.ui.theme.FlashLearnThemeTokens,
    onOpen: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpen),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(tokens.borderThin, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(tokens.contentGap),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Outlined.MoreVert,
                "گزینه‌ها",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(tokens.libraryWordIconSize)
            )
            
            Spacer(Modifier.width(tokens.compactGap))
            
            DifficultyPill(item.difficulty, tokens)
            
            Spacer(Modifier.weight(1f))
            
            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        item.source?.text ?: "—",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(Modifier.width(tokens.microGap))
                    Text(languagePair.source.flag, style = MaterialTheme.typography.titleMedium)
                }
                
                Spacer(Modifier.height(tokens.tinyGap))
                
                if (item.targets.isEmpty()) {
                    Text(
                        "—",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    item.targets.forEachIndexed { index, target ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                target.text,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(Modifier.width(tokens.microGap))
                            Text(languagePair.target.flag, style = MaterialTheme.typography.titleMedium)
                            if (index < item.targets.lastIndex) {
                                Text("،", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
                
                Spacer(Modifier.height(tokens.tinyGap))
                
                item.category?.let {
                    Text(
                        it.name,
                        color = MaterialTheme.colorScheme.secondary,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
            
            Spacer(Modifier.width(tokens.contentGap))
            
            Icon(
                Icons.Outlined.StarBorder,
                "موردعلاقه",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(tokens.libraryFavoriteIconSize)
            )
        }
    }
}

@Composable
private fun DifficultyPill(
    difficulty: VocabularyDifficulty?,
    tokens: com.flashlearn.app.ui.theme.FlashLearnThemeTokens
) {
    val (label, foreground) = when (difficulty) {
        VocabularyDifficulty.EASY, null -> "آسان" to MaterialTheme.colorScheme.primary
        VocabularyDifficulty.MEDIUM -> "متوسط" to MaterialTheme.colorScheme.error
        VocabularyDifficulty.HARD -> "سخت" to MaterialTheme.colorScheme.error
        VocabularyDifficulty.VERY_HARD -> "خیلی سخت" to MaterialTheme.colorScheme.error
    }
    
    val background = foreground.copy(alpha = 0.10f)
    
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = background
    ) {
        Text(
            label,
            color = foreground,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(
                horizontal = tokens.libraryDifficultyHorizontalPadding,
                vertical = tokens.libraryDifficultyVerticalPadding
            )
        )
    }
}

private fun toFaDigits(value: Int): String = value.toString().map { digit ->
    when (digit) {
        '0' -> '۰'
        '1' -> '۱'
        '2' -> '۲'
        '3' -> '۳'
        '4' -> '۴'
        '5' -> '۵'
        '6' -> '۶'
        '7' -> '۷'
        '8' -> '۸'
        '9' -> '۹'
        else -> digit
    }
}.joinToString("")
