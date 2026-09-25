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
import androidx.compose.ui.unit.dp
import com.flashlearn.app.BuildConfig
import com.flashlearn.app.ui.theme.LocalFlashLearnThemeTokens

@Composable
fun AboutScreen(onBack: () -> Unit) {
    val tokens = LocalFlashLearnThemeTokens.current
    val context = LocalContext.current
    
    val openGithub = {
        context.startActivity(
            Intent(Intent.ACTION_VIEW, Uri.parse(BuildConfig.APP_GITHUB_URL))
        )
    }
    
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(tokens.contentGap * 2),
            verticalArrangement = Arrangement.spacedBy(tokens.contentGap * 1.5f)
        ) {
            Text("درباره برنامه", style = MaterialTheme.typography.headlineSmall)
            
            // معلومات اساسی
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier
                        .padding(tokens.contentGap * 2.25f),
                    verticalArrangement = Arrangement.spacedBy(tokens.contentGap * 1.125f)
                ) {
                    Text(
                        "FlashLearn",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Text(
                        "FlashLearn برای یادگیری واژگان اسپانیایی و فارسی ساخته شده و کاملاً آفلاین کار می‌کند. " +
                        "داشبورد آمار، نمودار فعالیت مرور با فیلترهای هفتگی، ماهانه، سه‌ماهه و همه، شاخص تعداد مرور، " +
                        "و محاسبه پیشرفت از اولین مرور واقعی را نیز در خود برنامه ارائه می‌دهد."
                    )
                    Text(
                        "واژه‌ها را می‌توان دستی یا گروهی اضافه کرد و با مرور روزانه، هفتگی و ماهانه تثبیت کرد. " +
                        "آزمون چهارگزینه‌ای، پیشرفت، آمار و پشتیبان‌گیری نیز در خود برنامه در دسترس هستند."
                    )
                }
            }
            
            // اطلاعات فنی
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier
                        .padding(tokens.contentGap * 2.25f),
                    verticalArrangement = Arrangement.spacedBy(tokens.contentGap * 1.125f)
                ) {
                    Text(
                        "اطلاعات فنی",
                        style = MaterialTheme.typography.titleLarge
                    )
                    InfoRow("نسخه", BuildConfig.VERSION_NAME)
                    InfoRow("شماره نسخه", BuildConfig.VERSION_CODE.toString())
                    InfoRow("سازنده", BuildConfig.APP_AUTHOR)
                    InfoRow("زبان برنامه‌نویسی", BuildConfig.APP_LANGUAGE)
                    InfoRow("نوع دیتابیس", BuildConfig.APP_DATABASE)
                    InfoRow("هوش مصنوعی همکار در ساخت", BuildConfig.APP_AI_ASSISTANT)
                    InfoRow("تاریخ Build", BuildConfig.APP_BUILD_DATE)
                    
                    Text(
                        "GitHub: ${BuildConfig.APP_GITHUB_URL}",
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = openGithub),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            // وضعیت نسخه
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier
                        .padding(tokens.contentGap * 2.25f),
                    verticalArrangement = Arrangement.spacedBy(tokens.contentGap * 0.875f)
                ) {
                    Text(
                        "وضعیت نسخه",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        "نسخه نصب‌شده: v${BuildConfig.VERSION_NAME} (شماره ${BuildConfig.VERSION_CODE})"
                    )
                    Text(
                        "تاریخ Build این نسخه: ${BuildConfig.APP_BUILD_DATE}"
                    )
                    Text(
                        "مشخصات فنی و اطلاعات نسخه از Build Configuration پروژه خوانده می‌شوند؛ " +
                        "بنابراین با هر Build جدید، نسخه و تاریخ Build به‌صورت خودکار به‌روز می‌شوند."
                    )
                    Text(
                        "برای تاریخچه کامل تغییرات، CHANGELOG مخزن پروژه مرجع است."
                    )
                }
            }
            
            // دکمه بازگشت
            Button(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("بازگشت")
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Text("$label: $value")
}
