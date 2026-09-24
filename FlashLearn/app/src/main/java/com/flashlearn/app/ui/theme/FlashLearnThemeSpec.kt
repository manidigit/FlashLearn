package com.flashlearn.app.ui.theme

import android.content.Context
import androidx.compose.ui.graphics.Color
import org.json.JSONObject

data class FlashLearnThemeSpec(
    val id: String, val name: String, val lightPrimary: Long, val darkPrimary: Long,
    val lightSecondary: Long, val darkSecondary: Long, val lightBackground: Long, val darkBackground: Long,
    val lightSurface: Long, val darkSurface: Long, val lightSurfaceVariant: Long, val darkSurfaceVariant: Long,
    val lightOnSurface: Long, val darkOnSurface: Long, val lightOnSurfaceVariant: Long, val darkOnSurfaceVariant: Long,
    val lightCard: Long = lightSurface, val darkCard: Long = darkSurface,
    val lightOutline: Long = 0xFFE1DDE7, val darkOutline: Long = 0xFF38333F,
    val gradientStart: Long = lightPrimary, val gradientEnd: Long = lightSecondary,
    val iconStyle: String = "outlined", val elevationScale: Float = 1f,
    val cornerSmall: Float = 12f, val cornerMedium: Float = 16f, val cornerLarge: Float = 24f,
    val typographyScale: Float = 1f, val densityScale: Float = 1f
) {
    fun toJson(): String = JSONObject().apply {
        put("formatVersion", FORMAT_VERSION); put("id", id); put("name", name)
        put("lightPrimary", hex(lightPrimary)); put("darkPrimary", hex(darkPrimary))
        put("lightSecondary", hex(lightSecondary)); put("darkSecondary", hex(darkSecondary))
        put("lightBackground", hex(lightBackground)); put("darkBackground", hex(darkBackground))
        put("lightSurface", hex(lightSurface)); put("darkSurface", hex(darkSurface))
        put("lightSurfaceVariant", hex(lightSurfaceVariant)); put("darkSurfaceVariant", hex(darkSurfaceVariant))
        put("lightOnSurface", hex(lightOnSurface)); put("darkOnSurface", hex(darkOnSurface))
        put("lightOnSurfaceVariant", hex(lightOnSurfaceVariant)); put("darkOnSurfaceVariant", hex(darkOnSurfaceVariant))
        put("lightCard", hex(lightCard)); put("darkCard", hex(darkCard))
        put("lightOutline", hex(lightOutline)); put("darkOutline", hex(darkOutline))
        put("gradientStart", hex(gradientStart)); put("gradientEnd", hex(gradientEnd))
        put("iconStyle", iconStyle); put("elevationScale", elevationScale)
        put("cornerSmall", cornerSmall); put("cornerMedium", cornerMedium); put("cornerLarge", cornerLarge)
        put("typographyScale", typographyScale); put("densityScale", densityScale)
    }.toString(2)

    companion object {
        const val FORMAT_VERSION = 2
        val MODERN_PURPLE = FlashLearnThemeSpec(
            "modern_purple", "مدرن بنفش", 0xFF7C3AED, 0xFF9B6CFF, 0xFF536DFE, 0xFF8191FF,
            0xFFF8F7FC, 0xFF0B0D12, 0xFFFFFFFF, 0xFF151820, 0xFFF0EDF6, 0xFF20232D,
            0xFF17141C, 0xFFF5F2F8, 0xFF68636F, 0xFFB7B2BE, 0xFFFFFFFF, 0xFF171820,
            0xFFE4DFEA, 0xFF36313E, 0xFF7C3AED, 0xFF536DFE, "outlined", 1f, 16f, 20f, 28f, 1f, 1f
        )
        val MODERN_MINIMAL = FlashLearnThemeSpec(
            id="modern_minimal", name="مدرن مینیمال", lightPrimary=0xFF2563EB, darkPrimary=0xFF60A5FA,
            lightSecondary=0xFF475569, darkSecondary=0xFF94A3B8, lightBackground=0xFFF8FAFC, darkBackground=0xFF0B0F14,
            lightSurface=0xFFFFFFFF, darkSurface=0xFF141A22, lightSurfaceVariant=0xFFF1F5F9, darkSurfaceVariant=0xFF202833,
            lightOnSurface=0xFF111827, darkOnSurface=0xFFF3F4F6, lightOnSurfaceVariant=0xFF64748B, darkOnSurfaceVariant=0xFFB8C1CE,
            lightCard=0xFFFFFFFF, darkCard=0xFF171D26, lightOutline=0xFFD8E0EA, darkOutline=0xFF344152,
            gradientStart=0xFF2563EB, gradientEnd=0xFF475569, iconStyle="outlined", elevationScale=0.9f,
            cornerSmall=10f, cornerMedium=14f, cornerLarge=20f, typographyScale=1f, densityScale=1f
        )
        val OCEAN_BLUE = MODERN_PURPLE.copy(id="ocean_blue", name="اقیانوس آبی", lightPrimary=0xFF1769E0, darkPrimary=0xFF69B7FF, lightSecondary=0xFF0891B2, darkSecondary=0xFF22D3EE, lightBackground=0xFFF4F9FF, darkBackground=0xFF07111D, lightSurfaceVariant=0xFFE8F2FF, darkSurfaceVariant=0xFF172A3D, gradientStart=0xFF1769E0, gradientEnd=0xFF08B6C9, lightOutline=0xFFD7E5F5, darkOutline=0xFF294257)
        val FRESH_GREEN = MODERN_PURPLE.copy(id="fresh_green", name="سبز تازه", lightPrimary=0xFF138A5B, darkPrimary=0xFF52D49A, lightSecondary=0xFF0D9488, darkSecondary=0xFF2DD4BF, lightBackground=0xFFF3FBF7, darkBackground=0xFF07130F, lightSurfaceVariant=0xFFE5F5EC, darkSurfaceVariant=0xFF172D25, gradientStart=0xFF138A5B, gradientEnd=0xFF0D9488)
        val SUNSET_ORANGE = MODERN_PURPLE.copy(id="sunset_orange", name="غروب گرم", lightPrimary=0xFFE4572E, darkPrimary=0xFFFF9A62, lightSecondary=0xFFF59E0B, darkSecondary=0xFFFBBF24, lightBackground=0xFFFFF8F4, darkBackground=0xFF160D08, lightSurfaceVariant=0xFFFFECE2, darkSurfaceVariant=0xFF332119, gradientStart=0xFFE4572E, gradientEnd=0xFFF59E0B)
        val MIDNIGHT = MODERN_PURPLE.copy(id="midnight", name="نیمه‌شب", lightPrimary=0xFF475569, darkPrimary=0xFFCBD5E1, lightSecondary=0xFF64748B, darkSecondary=0xFF94A3B8, lightBackground=0xFFF7F8FA, darkBackground=0xFF05070B, lightSurfaceVariant=0xFFE9EDF2, darkSurfaceVariant=0xFF151B24, gradientStart=0xFF334155, gradientEnd=0xFF0F172A)
        val LUXURY = FlashLearnThemeSpec(
            id = "luxury",
            name = "لاکچری طلایی",
            // Light mode (warm cream + deep gold)
            lightPrimary = 0xFFC9A227,
            darkPrimary = 0xFFD4AF37,
            lightSecondary = 0xFFB8860B,
            darkSecondary = 0xFFE8C547,
            lightBackground = 0xFFF8F4EC,
            darkBackground = 0xFF0F1419,          // deep charcoal
            lightSurface = 0xFFFFFDF8,
            darkSurface = 0xFF1A2332,             // elevated dark surface
            lightSurfaceVariant = 0xFFF2EBDD,
            darkSurfaceVariant = 0xFF243044,
            lightOnSurface = 0xFF1A140A,
            darkOnSurface = 0xFFF5F0E6,           // warm cream text
            lightOnSurfaceVariant = 0xFF6B5E4A,
            darkOnSurfaceVariant = 0xFFA8B0BC,    // soft secondary text
            lightCard = 0xFFFFFCF5,
            darkCard = 0xFF1A2332,
            lightOutline = 0xFFE6D5B5,
            darkOutline = 0xFF3D4A5C,             // subtle dark border
            gradientStart = 0xFFC9A227,
            gradientEnd = 0xFFD4AF37,
            iconStyle = "filled",
            elevationScale = 1.35f,               // deeper shadows for luxury
            cornerSmall = 14f,
            cornerMedium = 20f,
            cornerLarge = 28f,
            typographyScale = 1.04f,
            densityScale = 0.97f
        )
        val ROSE_GOLD = MODERN_PURPLE.copy(id="rose_gold", name="رزگلد", lightPrimary=0xFFB45A6A, darkPrimary=0xFFFFA7B4, lightSecondary=0xFFC47B83, darkSecondary=0xFFFFC4CC, lightBackground=0xFFFFF7F8, darkBackground=0xFF14090C, lightSurfaceVariant=0xFFF8E8EB, darkSurfaceVariant=0xFF2C171C, gradientStart=0xFFB45A6A, gradientEnd=0xFFD8A07C, cornerSmall=16f, cornerMedium=22f, cornerLarge=30f, iconStyle="outlined", typographyScale=1.02f)
        val FOREST = MODERN_PURPLE.copy(id="forest", name="جنگل عمیق", lightPrimary=0xFF276749, darkPrimary=0xFF7AD6A5, lightSecondary=0xFF4F772D, darkSecondary=0xFFA8D58A, lightBackground=0xFFF4F8F2, darkBackground=0xFF07100A, lightSurfaceVariant=0xFFE6EFE3, darkSurfaceVariant=0xFF17251A, gradientStart=0xFF276749, gradientEnd=0xFF4F772D, iconStyle="filled")
        val BUILT_IN = listOf(MODERN_PURPLE, MODERN_MINIMAL, OCEAN_BLUE, FRESH_GREEN, SUNSET_ORANGE, MIDNIGHT, LUXURY, ROSE_GOLD, FOREST)

        fun fromJson(raw:String):FlashLearnThemeSpec {
            val j=JSONObject(raw); require(j.optInt("formatVersion")==FORMAT_VERSION)
            fun c(k:String,d:Long)=parseColor(j.optString(k,hex(d)))
            return FlashLearnThemeSpec(j.getString("id").take(80),j.getString("name").take(80),
                c("lightPrimary",MODERN_PURPLE.lightPrimary),c("darkPrimary",MODERN_PURPLE.darkPrimary),
                c("lightSecondary",MODERN_PURPLE.lightSecondary),c("darkSecondary",MODERN_PURPLE.darkSecondary),
                c("lightBackground",MODERN_PURPLE.lightBackground),c("darkBackground",MODERN_PURPLE.darkBackground),
                c("lightSurface",MODERN_PURPLE.lightSurface),c("darkSurface",MODERN_PURPLE.darkSurface),
                c("lightSurfaceVariant",MODERN_PURPLE.lightSurfaceVariant),c("darkSurfaceVariant",MODERN_PURPLE.darkSurfaceVariant),
                c("lightOnSurface",MODERN_PURPLE.lightOnSurface),c("darkOnSurface",MODERN_PURPLE.darkOnSurface),
                c("lightOnSurfaceVariant",MODERN_PURPLE.lightOnSurfaceVariant),c("darkOnSurfaceVariant",MODERN_PURPLE.darkOnSurfaceVariant),
                c("lightCard",MODERN_PURPLE.lightCard),c("darkCard",MODERN_PURPLE.darkCard),
                c("lightOutline",MODERN_PURPLE.lightOutline),c("darkOutline",MODERN_PURPLE.darkOutline),
                c("gradientStart",MODERN_PURPLE.gradientStart),c("gradientEnd",MODERN_PURPLE.gradientEnd),
                j.optString("iconStyle","outlined").ifBlank{"outlined"},j.optDouble("elevationScale",1.0).toFloat().coerceIn(.7f,1.8f),
                j.optDouble("cornerSmall",12.0).toFloat().coerceIn(0f,40f),j.optDouble("cornerMedium",16.0).toFloat().coerceIn(0f,48f),
                j.optDouble("cornerLarge",24.0).toFloat().coerceIn(0f,56f),j.optDouble("typographyScale",1.0).toFloat().coerceIn(.85f,1.25f),j.optDouble("densityScale",1.0).toFloat().coerceIn(.85f,1.15f))
        }
        fun loadCustom(context:Context):List<FlashLearnThemeSpec> = context.getSharedPreferences("flashlearn_themes",0).getStringSet("custom",emptySet()).orEmpty().mapNotNull{runCatching{fromJson(it)}.getOrNull()}
        fun saveCustom(context:Context,spec:FlashLearnThemeSpec){val p=context.getSharedPreferences("flashlearn_themes",0);val s=p.getStringSet("custom",emptySet()).orEmpty().toMutableSet();s.removeIf{runCatching{fromJson(it).id==spec.id}.getOrDefault(false)};s.add(spec.toJson());p.edit().putStringSet("custom",s).apply()}
        private fun parseColor(v:String):Long{val x=v.removePrefix("#");return (if(x.length==6)"FF$x" else x).toLong(16)}
        private fun hex(v:Long):String="#" + v.toString(16).padStart(8,'0')
    }
}
internal fun Long.asComposeColor()=Color(this)
