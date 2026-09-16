package com.flashlearn.app.ui.theme

import android.content.Context
import androidx.compose.ui.graphics.Color
import org.json.JSONObject

data class FlashLearnThemeSpec(
    val id: String,
    val name: String,
    val lightPrimary: Long,
    val darkPrimary: Long,
    val lightSecondary: Long,
    val darkSecondary: Long,
    val lightBackground: Long,
    val darkBackground: Long,
    val lightSurface: Long,
    val darkSurface: Long,
    val lightSurfaceVariant: Long,
    val darkSurfaceVariant: Long,
    val lightOnSurface: Long,
    val darkOnSurface: Long,
    val lightOnSurfaceVariant: Long,
    val darkOnSurfaceVariant: Long,
    val cornerSmall: Float = 14f,
    val cornerMedium: Float = 18f,
    val cornerLarge: Float = 24f,
    val typographyScale: Float = 1f,
    val densityScale: Float = 1f
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
        put("cornerSmall", cornerSmall); put("cornerMedium", cornerMedium); put("cornerLarge", cornerLarge)
        put("typographyScale", typographyScale); put("densityScale", densityScale)
    }.toString(2)

    companion object {
        const val FORMAT_VERSION = 1
        val MODERN_PURPLE = FlashLearnThemeSpec("modern_purple", "مدرن بنفش", 0xFF7C3AED, 0xFF9B6CFF, 0xFF536DFE, 0xFF8191FF, 0xFFF8F7FC, 0xFF0B0D12, 0xFFFFFFFF, 0xFF151820, 0xFFF0EDF6, 0xFF20232D, 0xFF17141C, 0xFFF5F2F8, 0xFF68636F, 0xFFB7B2BE)
        val OCEAN_BLUE = MODERN_PURPLE.copy(id="ocean_blue", name="اقیانوس آبی", lightPrimary=0xFF2563EB, darkPrimary=0xFF60A5FA, lightSecondary=0xFF0EA5E9, darkSecondary=0xFF38BDF8, lightBackground=0xFFF5F9FF, lightSurfaceVariant=0xFFEAF2FF, darkSurfaceVariant=0xFF1D2A3D)
        val FRESH_GREEN = MODERN_PURPLE.copy(id="fresh_green", name="سبز تازه", lightPrimary=0xFF16A34A, darkPrimary=0xFF4ADE80, lightSecondary=0xFF0D9488, darkSecondary=0xFF2DD4BF, lightBackground=0xFFF5FBF7, lightSurfaceVariant=0xFFE8F5EC, darkSurfaceVariant=0xFF1D2B24)
        val SUNSET_ORANGE = MODERN_PURPLE.copy(id="sunset_orange", name="غروب نارنجی", lightPrimary=0xFFEA580C, darkPrimary=0xFFFB923C, lightSecondary=0xFFF59E0B, darkSecondary=0xFFFBBF24, lightBackground=0xFFFFF8F3, lightSurfaceVariant=0xFFFFEDE0, darkSurfaceVariant=0xFF30251E)
        val MIDNIGHT = MODERN_PURPLE.copy(id="midnight", name="نیمه‌شب", lightPrimary=0xFF475569, darkPrimary=0xFFCBD5E1, lightSecondary=0xFF64748B, darkSecondary=0xFF94A3B8, lightBackground=0xFFF7F8FA, darkBackground=0xFF070A0F, lightSurfaceVariant=0xFFE9EDF2, darkSurfaceVariant=0xFF161C26)
        val BUILT_IN = listOf(MODERN_PURPLE, OCEAN_BLUE, FRESH_GREEN, SUNSET_ORANGE, MIDNIGHT)

        fun fromJson(raw: String): FlashLearnThemeSpec {
            val j=JSONObject(raw); require(j.optInt("formatVersion") == FORMAT_VERSION)
            fun c(k:String,d:Long)=parseColor(j.optString(k,hex(d)))
            return FlashLearnThemeSpec(j.getString("id").take(80),j.getString("name").take(80),c("lightPrimary",MODERN_PURPLE.lightPrimary),c("darkPrimary",MODERN_PURPLE.darkPrimary),c("lightSecondary",MODERN_PURPLE.lightSecondary),c("darkSecondary",MODERN_PURPLE.darkSecondary),c("lightBackground",MODERN_PURPLE.lightBackground),c("darkBackground",MODERN_PURPLE.darkBackground),c("lightSurface",MODERN_PURPLE.lightSurface),c("darkSurface",MODERN_PURPLE.darkSurface),c("lightSurfaceVariant",MODERN_PURPLE.lightSurfaceVariant),c("darkSurfaceVariant",MODERN_PURPLE.darkSurfaceVariant),c("lightOnSurface",MODERN_PURPLE.lightOnSurface),c("darkOnSurface",MODERN_PURPLE.darkOnSurface),c("lightOnSurfaceVariant",MODERN_PURPLE.lightOnSurfaceVariant),c("darkOnSurfaceVariant",MODERN_PURPLE.darkOnSurfaceVariant),j.optDouble("cornerSmall",14.0).toFloat().coerceIn(0f,40f),j.optDouble("cornerMedium",18.0).toFloat().coerceIn(0f,48f),j.optDouble("cornerLarge",24.0).toFloat().coerceIn(0f,56f),j.optDouble("typographyScale",1.0).toFloat().coerceIn(.85f,1.25f),j.optDouble("densityScale",1.0).toFloat().coerceIn(.85f,1.15f))
        }
        fun loadCustom(context:Context):List<FlashLearnThemeSpec> = context.getSharedPreferences("flashlearn_themes",0).getStringSet("custom",emptySet()).orEmpty().mapNotNull{runCatching{fromJson(it)}.getOrNull()}
        fun saveCustom(context:Context,spec:FlashLearnThemeSpec){val p=context.getSharedPreferences("flashlearn_themes",0);val s=p.getStringSet("custom",emptySet()).orEmpty().toMutableSet();s.removeIf{runCatching{fromJson(it).id==spec.id}.getOrDefault(false)};s.add(spec.toJson());p.edit().putStringSet("custom",s).apply()}
        private fun parseColor(v:String):Long{val x=v.removePrefix("#");return (if(x.length==6)"FF$x" else x).toLong(16)}
        private fun hex(v:Long)="#${v.toString(16).padStart(8,'0')}"
    }
}

internal fun Long.asComposeColor()=Color(this)
