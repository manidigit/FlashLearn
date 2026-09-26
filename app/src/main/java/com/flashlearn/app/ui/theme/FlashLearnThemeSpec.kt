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
    val typographyScale: Float = 1f, val densityScale: Float = 1f,
    val spacingScale: Float = 1f
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
        put("typographyScale", typographyScale); put("densityScale", densityScale); put("spacingScale", spacingScale)
    }.toString(2)

    companion object {
        const val FORMAT_VERSION = 3

        val MODERN_MINIMAL = FlashLearnThemeSpec(
            id="modern_minimal", name="مدرن مینیمال", lightPrimary=0xFF2563EB, darkPrimary=0xFF60A5FA,
            lightSecondary=0xFF475569, darkSecondary=0xFF94A3B8, lightBackground=0xFFF8FAFC, darkBackground=0xFF0B0F14,
            lightSurface=0xFFFFFFFF, darkSurface=0xFF141A22, lightSurfaceVariant=0xFFF1F5F9, darkSurfaceVariant=0xFF202833,
            lightOnSurface=0xFF111827, darkOnSurface=0xFFF3F4F6, lightOnSurfaceVariant=0xFF64748B, darkOnSurfaceVariant=0xFFB8C1CE,
            lightCard=0xFFFFFFFF, darkCard=0xFF171D26, lightOutline=0xFFD8E0EA, darkOutline=0xFF344152,
            gradientStart=0xFF2563EB, gradientEnd=0xFF475569, iconStyle="outlined", elevationScale=0.9f,
            cornerSmall=10f, cornerMedium=14f, cornerLarge=20f, typographyScale=1f, densityScale=1f, spacingScale=1f
        )

        val GROK = FlashLearnThemeSpec(
            id = "grok", name = "گروک",
            lightPrimary = 0xFFC9A227, darkPrimary = 0xFFD4AF37,
            lightSecondary = 0xFFB8860B, darkSecondary = 0xFFE8C547,
            lightBackground = 0xFFF8F4EC, darkBackground = 0xFF0F1419,
            lightSurface = 0xFFFFFDF8, darkSurface = 0xFF1A2332,
            lightSurfaceVariant = 0xFFF2EBDD, darkSurfaceVariant = 0xFF243044,
            lightOnSurface = 0xFF1A140A, darkOnSurface = 0xFFF5F0E6,
            lightOnSurfaceVariant = 0xFF6B5E4A, darkOnSurfaceVariant = 0xFFA8B0BC,
            lightCard = 0xFFFFFCF5, darkCard = 0xFF1A2332,
            lightOutline = 0xFFE6D5B5, darkOutline = 0xFF3D4A5C,
            gradientStart = 0xFFC9A227, gradientEnd = 0xFFD4AF37,
            iconStyle = "filled", elevationScale = 1.35f,
            cornerSmall = 14f, cornerMedium = 20f, cornerLarge = 28f,
            typographyScale = 1.04f, densityScale = 0.97f, spacingScale = 1f
        )

        val CLAUD = FlashLearnThemeSpec(
            id = "claud", name = "کلاد",
            lightPrimary = 0xFF2C5F4E, darkPrimary = 0xFF5DAA92,
            lightSecondary = 0xFF4A7C6E, darkSecondary = 0xFF7BC9B3,
            lightBackground = 0xFFFAF9F7, darkBackground = 0xFF1A1A1A,
            lightSurface = 0xFFFFFFFF, darkSurface = 0xFF252525,
            lightSurfaceVariant = 0xFFF3F2F0, darkSurfaceVariant = 0xFF2F2F2F,
            lightOnSurface = 0xFF1A1A1A, darkOnSurface = 0xFFF5F5F5,
            lightOnSurfaceVariant = 0xFF6B7A76, darkOnSurfaceVariant = 0xFFA8C4BB,
            lightCard = 0xFFFFFFFF, darkCard = 0xFF252525,
            lightOutline = 0xFFDDD8D4, darkOutline = 0xFF3A3A3A,
            gradientStart = 0xFF2C5F4E, gradientEnd = 0xFF4A7C6E,
            iconStyle = "outlined", elevationScale = 0.8f,
            cornerSmall = 12f, cornerMedium = 16f, cornerLarge = 24f,
            typographyScale = 1f, densityScale = 1f, spacingScale = 1f
        )

        val SPARK = FlashLearnThemeSpec(
            id = "spark", name = "جرقه",
            lightPrimary = 0xFF58CC02, darkPrimary = 0xFF78E633,
            lightSecondary = 0xFF7C4DFF, darkSecondary = 0xFFA98BFF,
            lightBackground = 0xFFF7F9F5, darkBackground = 0xFF111713,
            lightSurface = 0xFFFFFFFF, darkSurface = 0xFF19221C,
            lightSurfaceVariant = 0xFFEFF6EC, darkSurfaceVariant = 0xFF223026,
            lightOnSurface = 0xFF24302A, darkOnSurface = 0xFFF2F7F3,
            lightOnSurfaceVariant = 0xFF6B756E, darkOnSurfaceVariant = 0xFFB8C4BB,
            lightCard = 0xFFFFFFFF, darkCard = 0xFF19221C,
            lightOutline = 0xFFD7E3D2, darkOutline = 0xFF344338,
            gradientStart = 0xFF58CC02, gradientEnd = 0xFF7C4DFF,
            iconStyle = "filled", elevationScale = 0.92f,
            cornerSmall = 14f, cornerMedium = 20f, cornerLarge = 24f,
            typographyScale = 1.02f, densityScale = 1f, spacingScale = 1f
        )

        /**
         * GTP is deliberately a fifth, visibly distinct design language:
         * compact spacing, sharper geometry, stronger elevation, filled actions,
         * violet/cyan contrast and slightly larger type.
         */
        val GTP = FlashLearnThemeSpec(
            id = "gtp", name = "GTP",
            lightPrimary = 0xFF7C3AED, darkPrimary = 0xFFA78BFA,
            lightSecondary = 0xFF06B6D4, darkSecondary = 0xFF22D3EE,
            lightBackground = 0xFFF6F3FF, darkBackground = 0xFF090711,
            lightSurface = 0xFFFFFFFF, darkSurface = 0xFF15101F,
            lightSurfaceVariant = 0xFFEDE7FF, darkSurfaceVariant = 0xFF21192F,
            lightOnSurface = 0xFF171225, darkOnSurface = 0xFFF7F2FF,
            lightOnSurfaceVariant = 0xFF6F6485, darkOnSurfaceVariant = 0xFFC4B9D6,
            lightCard = 0xFFFCFAFF, darkCard = 0xFF191222,
            lightOutline = 0xFFD8CFF0, darkOutline = 0xFF49375E,
            gradientStart = 0xFF7C3AED, gradientEnd = 0xFF06B6D4,
            iconStyle = "filled", elevationScale = 1.55f,
            cornerSmall = 6f, cornerMedium = 12f, cornerLarge = 18f,
            typographyScale = 1.07f, densityScale = 0.94f, spacingScale = 0.88f
        )

        val BUILT_IN = listOf(GROK, CLAUD, MODERN_MINIMAL, SPARK, GTP)

        fun fromJson(raw:String):FlashLearnThemeSpec {
            val j=JSONObject(raw); require(j.optInt("formatVersion") in 2..FORMAT_VERSION)
            fun c(k:String,d:Long)=parseColor(j.optString(k,hex(d)))
            return FlashLearnThemeSpec(
                j.getString("id").take(80), j.getString("name").take(80),
                c("lightPrimary",MODERN_MINIMAL.lightPrimary), c("darkPrimary",MODERN_MINIMAL.darkPrimary),
                c("lightSecondary",MODERN_MINIMAL.lightSecondary), c("darkSecondary",MODERN_MINIMAL.darkSecondary),
                c("lightBackground",MODERN_MINIMAL.lightBackground), c("darkBackground",MODERN_MINIMAL.darkBackground),
                c("lightSurface",MODERN_MINIMAL.lightSurface), c("darkSurface",MODERN_MINIMAL.darkSurface),
                c("lightSurfaceVariant",MODERN_MINIMAL.lightSurfaceVariant), c("darkSurfaceVariant",MODERN_MINIMAL.darkSurfaceVariant),
                c("lightOnSurface",MODERN_MINIMAL.lightOnSurface), c("darkOnSurface",MODERN_MINIMAL.darkOnSurface),
                c("lightOnSurfaceVariant",MODERN_MINIMAL.lightOnSurfaceVariant), c("darkOnSurfaceVariant",MODERN_MINIMAL.darkOnSurfaceVariant),
                c("lightCard",MODERN_MINIMAL.lightCard), c("darkCard",MODERN_MINIMAL.darkCard),
                c("lightOutline",MODERN_MINIMAL.lightOutline), c("darkOutline",MODERN_MINIMAL.darkOutline),
                c("gradientStart",MODERN_MINIMAL.gradientStart), c("gradientEnd",MODERN_MINIMAL.gradientEnd),
                j.optString("iconStyle","outlined").ifBlank{"outlined"},
                j.optDouble("elevationScale",1.0).toFloat().coerceIn(.7f,1.8f),
                j.optDouble("cornerSmall",12.0).toFloat().coerceIn(0f,40f),
                j.optDouble("cornerMedium",16.0).toFloat().coerceIn(0f,48f),
                j.optDouble("cornerLarge",24.0).toFloat().coerceIn(0f,56f),
                j.optDouble("typographyScale",1.0).toFloat().coerceIn(.85f,1.25f),
                j.optDouble("densityScale",1.0).toFloat().coerceIn(.85f,1.15f),
                j.optDouble("spacingScale",1.0).toFloat().coerceIn(.75f,1.25f)
            )
        }

        fun loadCustom(context:Context):List<FlashLearnThemeSpec> =
            context.getSharedPreferences("flashlearn_themes",0)
                .getStringSet("custom",emptySet()).orEmpty()
                .mapNotNull{runCatching{fromJson(it)}.getOrNull()}

        fun saveCustom(context:Context,spec:FlashLearnThemeSpec){
            val p=context.getSharedPreferences("flashlearn_themes",0)
            val s=p.getStringSet("custom",emptySet()).orEmpty().toMutableSet()
            s.removeIf{runCatching{fromJson(it).id==spec.id}.getOrDefault(false)}
            s.add(spec.toJson())
            p.edit().putStringSet("custom",s).apply()
        }

        private fun parseColor(v:String):Long{
            val x=v.removePrefix("#")
            return (if(x.length==6)"FF$x" else x).toLong(16)
        }
        private fun hex(v:Long):String="#" + v.toString(16).padStart(8,'0')
    }
}

internal fun Long.asComposeColor()=Color(this)
