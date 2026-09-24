package com.flashlearn.app.ui.help

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.flashlearn.app.R
import com.flashlearn.app.ui.components.FlashLearnScreenHeader
import com.flashlearn.app.ui.theme.LocalFlashLearnThemeTokens

@Composable
fun HelpScreen(onBack: () -> Unit) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = LocalFlashLearnThemeTokens.current.screenPadding, vertical = LocalFlashLearnThemeTokens.current.screenVerticalPadding),
        verticalArrangement = Arrangement.spacedBy(LocalFlashLearnThemeTokens.current.contentGap)
    ) {
        FlashLearnScreenHeader(title = stringResource(R.string.help_title), onBack = onBack)

        HelpSection(stringResource(R.string.help_get_started_title), stringResource(R.string.help_get_started_body))
        HelpSection(stringResource(R.string.help_add_word_title), stringResource(R.string.help_add_word_body))
        HelpSection(stringResource(R.string.help_review_title), stringResource(R.string.help_review_body))
        HelpSection(stringResource(R.string.help_library_title), stringResource(R.string.help_library_body))
        HelpSection(stringResource(R.string.help_settings_title_short), stringResource(R.string.help_settings_body))
        HelpSection(stringResource(R.string.help_tts_title), stringResource(R.string.help_tts_body))

        Text(
            stringResource(R.string.help_boundary),
            style = MaterialTheme.typography.bodySmall,
            color = tokens.onSurfaceVariant,
            textAlign = TextAlign.End,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun HelpSection(title: String, body: String) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(body, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
