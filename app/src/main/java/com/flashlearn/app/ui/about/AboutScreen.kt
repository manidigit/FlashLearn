package com.flashlearn.app.ui.about

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.flashlearn.app.BuildConfig
import com.flashlearn.app.R
import com.flashlearn.app.ui.theme.LocalFlashLearnThemeTokens

@Composable
fun AboutScreen(onBack: () -> Unit) {
    val tokens = LocalFlashLearnThemeTokens.current
    val context = LocalContext.current
    val openGithub = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(BuildConfig.APP_GITHUB_URL))) }
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(tokens.contentGap * 2), verticalArrangement = Arrangement.spacedBy(tokens.contentGap * 1.5f)) {
            Text(stringResource(R.string.about_title), style = MaterialTheme.typography.headlineSmall)
            Card(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(modifier = Modifier.padding(tokens.contentGap * 2.25f), verticalArrangement = Arrangement.spacedBy(tokens.contentGap * 1.125f)) {
                    Text("FlashLearn", style = MaterialTheme.typography.headlineMedium)
                    Text(stringResource(R.string.about_description))
                    Text(stringResource(R.string.about_features))
                }
            }
            Card(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(modifier = Modifier.padding(tokens.contentGap * 2.25f), verticalArrangement = Arrangement.spacedBy(tokens.contentGap * 1.125f)) {
                    Text(stringResource(R.string.about_technical_info), style = MaterialTheme.typography.titleLarge)
                    InfoRow(stringResource(R.string.about_version), BuildConfig.VERSION_NAME)
                    InfoRow(stringResource(R.string.about_version_code), BuildConfig.VERSION_CODE.toString())
                    InfoRow(stringResource(R.string.about_author), BuildConfig.APP_AUTHOR)
                    InfoRow(stringResource(R.string.about_language), BuildConfig.APP_LANGUAGE)
                    InfoRow(stringResource(R.string.about_database), BuildConfig.APP_DATABASE)
                    InfoRow(stringResource(R.string.about_ai_assistant), BuildConfig.APP_AI_ASSISTANT)
                    InfoRow(stringResource(R.string.about_build_date), BuildConfig.APP_BUILD_DATE)
                    Text(stringResource(R.string.about_github, BuildConfig.APP_GITHUB_URL), modifier = Modifier.fillMaxWidth().clickable(onClick = openGithub), color = MaterialTheme.colorScheme.primary)
                }
            }
            Card(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(modifier = Modifier.padding(tokens.contentGap * 2.25f), verticalArrangement = Arrangement.spacedBy(tokens.contentGap * 0.875f)) {
                    Text(stringResource(R.string.about_release_status), style = MaterialTheme.typography.titleLarge)
                    Text(stringResource(R.string.about_installed_version, BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE))
                    Text(stringResource(R.string.about_build_date_current, BuildConfig.APP_BUILD_DATE))
                    Text(stringResource(R.string.about_build_configuration))
                    Text(stringResource(R.string.about_changelog_reference))
                }
            }
            Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.action_back)) }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) { Text("$label: $value") }
