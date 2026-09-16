package com.flashlearn.app.ui.about

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.flashlearn.app.BuildConfig

@Composable
fun AboutScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val openGithub = {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(BuildConfig.APP_GITHUB_URL)))
    }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("درباره برنامه", style = MaterialTheme.typography.headlineSmall)

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                Text("FlashLearn", style = MaterialTheme.typography.headlineMedium)
                Text("FlashLearn برای یادگیری واژگان اسپانیایی و فارسی ساخته شده و کاملاً آفلاین کار می‌کند.")
                Text("واژه‌ها را می‌توان دستی یا گروهی اضافه کرد و با مرور روزانه، هفتگی و ماهانه تثبیت کرد. آزمون چهارگزینه‌ای، پیشرفت، آمار و پشتیبان‌گیری نیز در خود برنامه در دسترس هستند.")

                Text("اطلاعات فنی", style = MaterialTheme.typography.titleLarge)
                InfoRow("نسخه", BuildConfig.VERSION_NAME)
                InfoRow("شماره نسخه", BuildConfig.VERSION_CODE.toString())
                InfoRow("سازنده", BuildConfig.APP_AUTHOR)
                InfoRow("زبان برنامه‌نویسی", BuildConfig.APP_LANGUAGE)
                InfoRow("نوع دیتابیس", BuildConfig.APP_DATABASE)
                InfoRow("هوش مصنوعی همکار در ساخت", BuildConfig.APP_AI_ASSISTANT)
                InfoRow("تاریخ Build", BuildConfig.APP_BUILD_DATE)
                Text(
                    "GitHub: ${BuildConfig.APP_GITHUB_URL}",
                    modifier = Modifier.fillMaxWidth().clickable(onClick = openGithub),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                Text("تاریخچه تغییرات", style = MaterialTheme.typography.titleLarge)
                Text("v5.77 — جلوگیری سراسری از تکرار یک کلمه در همان روز در Random، Daily، Weekly، Monthly و Learned.")
                Text("v5.76 — اصلاحات رابط کاربری، تم، آیکن‌ها، چگالی و سطوح کارت‌ها.")
                Text("v5.73 — بهینه‌سازی مرور و آمار برای کتابخانه‌های بزرگ، محدودیت ۳۰ کارت در جلسه، بهبود انتخاب Distractor و بازطراحی تجربه آزمون چهارگزینه‌ای.")
                Text("برای تاریخچه کامل، CHANGELOG مخزن پروژه مرجع است.")
            }
        }

        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("بازگشت") }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Text("$label: $value")
}
