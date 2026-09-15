package com.flashlearn.app.ui.about

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
import androidx.compose.ui.unit.dp

@Composable
fun AboutScreen(onBack: () -> Unit) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("درباره برنامه", style = MaterialTheme.typography.headlineSmall)
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("FlashLearn", style = MaterialTheme.typography.headlineMedium)
                Text("FlashLearn برای یادگیری واژگان اسپانیایی و فارسی ساخته شده و کاملاً آفلاین کار می‌کند.")
                Text("واژه‌ها را می‌توان دستی یا گروهی اضافه کرد و با مرور روزانه، هفتگی و ماهانه تثبیت کرد. آزمون چهارگزینه‌ای، پیشرفت، آمار و پشتیبان‌گیری نیز در خود برنامه در دسترس هستند.")
                Text("نسخه: 5.73")
                Text("تاریخ: 2026-09-15")
                Text("سازنده: ManiDigit")
            }
        }
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                Text("تاریخچه تغییرات", style = MaterialTheme.typography.titleLarge)
                Text("v5.73 — بهینه‌سازی مرور و آمار برای کتابخانه‌های بزرگ، محدودیت ۳۰ کارت در جلسه، بهبود انتخاب Distractor و بازطراحی تجربه آزمون چهارگزینه‌ای.")
                Text("v5.71 — پشتیبانی از بازیابی FULL قدیمی و سخت‌سازی مسیر Update.")
                Text("v5.70 — سازگاری بازیابی FULL قدیمی و پوشش تست اندروید.")
                Text("برای تاریخچه کامل، CHANGELOG مخزن پروژه مرجع است.")
            }
        }
        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("بازگشت") }
    }
}
