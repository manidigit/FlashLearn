package com.flashlearn.app.ui.theme

import android.content.Context
import androidx.compose.ui.graphics.Color
import org.json.JSONObject

data class ThemeDesign(
    val metrics: Map<String, Float>,
    val buttonStyle: ButtonStyle = ButtonStyle.FILLED,
    val navStyle: NavigationStyle = NavigationStyle.STANDARD,
    val statsLayout: StatsLayoutStrategy = StatsLayoutStrategy.GRID_2X2,
    val reviewsLayout: ReviewsLayoutStrategy = ReviewsLayoutStrategy.VERTICAL_ROWS,
    val libraryLayout: LibraryLayoutStrategy = LibraryLayoutStrategy.GRID_2_COLUMNS,
    val reviewPresentation: ReviewPresentation = ReviewPresentation.STANDARD,
    val showReviewOrnaments: Boolean = true,
    val lightOnPrimary: Long = 0xFFFFFFFF,
    val darkOnPrimary: Long = 0xFFFFFFFF,
    val lightSuccess: Long = 0xFF138A5B,
    val darkSuccess: Long = 0xFF52D49A,
    val lightWarning: Long = 0xFFF59E0B,
    val darkWarning: Long = 0xFFFBBF24,
    val lightError: Long = 0xFFD92D48,
    val darkError: Long = 0xFFFF8A9A,
    val lightInfo: Long = 0xFF536DFE,
    val darkInfo: Long = 0xFF7C8CFF,
    val iconStyle: IconStyle = IconStyle.OUTLINED,
    val activeIconStyle: IconStyle = IconStyle.FILLED,
    val iconSizeScale: Float = 1f,
    val activeIconSizeScale: Float = 1.08f,
    val navIndicatorAlpha: Float = .16f
) {
    fun metric(name: String): Float = metrics[name] ?: error("Theme design metric missing: $name")
    companion object {
        private val BASE = mapOf(
            "screenPadding" to 20f,"screenVerticalPadding" to 12f,"contentPadding" to 16f,"cardPadding" to 16f,
            "compactPadding" to 8f,"tinyGap" to 4f,"microGap" to 6f,"contentGap" to 12f,"compactGap" to 8f,
            "sectionGap" to 16f,"itemGap" to 8f,"headerHeight" to 58f,"headerPadding" to 10f,"controlHeight" to 52f,
            "buttonHeight" to 52f,"fieldHeight" to 52f,"cardMinHeight" to 84f,"statCardHeight" to 132f,
            "largeChoiceHeight" to 96f,"mediumChoiceHeight" to 72f,"chartHeight" to 210f,"progressTrackHeight" to 9f,
            "borderThin" to 1f,"borderStrong" to 2f,"borderEmphasis" to 3f,"iconTileSize" to 48f,"choiceIconSize" to 30f,
            "iconSmall" to 20f,"iconMedium" to 24f,"iconLarge" to 28f,"navHeight" to 76f,"libraryHeaderHeight" to 58f,
            "librarySearchHeight" to 58f,"libraryStatIconSize" to 42f,"libraryWordIconSize" to 25f,"libraryFavoriteIconSize" to 31f,
            "libraryDifficultyHorizontalPadding" to 16f,"libraryDifficultyVerticalPadding" to 7f,"reviewHeaderHeight" to 92f,
            "reviewHeaderGap" to 8f,"reviewBackButtonSize" to 50f,"reviewBackIcon" to 28f,"reviewBackElevation" to 2f,
            "reviewOrnamentLine" to 36f,"reviewOrnamentHeight" to 2f,"reviewOrnamentIcon" to 12f,"reviewTinyGap" to 3f,
            "reviewSectionGap" to 7f,"reviewItemGap" to 7f,"reviewChoiceHeight" to 58f,"reviewCategoryHeight" to 56f,
            "reviewDifficultyHeight" to 56f,"reviewQuizHeight" to 56f,"reviewFullChoiceHeight" to 48f,"reviewCountHeight" to 44f,
            "reviewCardPadding" to 12f,"reviewCompactPadding" to 8f,"reviewContentPadding" to 14f,"reviewIconLarge" to 26f,
            "reviewIconMedium" to 24f,"reviewCategoryIconTile" to 38f,"reviewSelectedBadgeInset" to 4f,"reviewSelectedBadgePadding" to 2f,
            "reviewSelectedBadgeIcon" to 12f,"reviewCardElevation" to 1.5f,"reviewButtonHeight" to 50f,"reviewPlayCircle" to 30f,
            "reviewPlayIcon" to 19f,"reviewNavHeight" to 70f,"cardElevationBase" to 4f,"dividerAlpha" to .65f,
            "reviewSelectedAlpha" to .10f,"cardBorderAlpha" to .40f,"cardBorderStrongAlpha" to .65f,"accentSurfaceAlpha" to .10f,
            "hierarchyBoost" to 1f,"homeHeroHeight" to 68f,"homeReviewHeight" to 68f,"homeCtaHeight" to 46f,"homeBottomGap" to 12f
        )
        private fun metrics(scale: Float, overrides: Map<String,Float> = emptyMap()) =
            BASE.mapValues { (_,v) -> v * scale }.toMutableMap().apply { putAll(overrides) }
        fun grok() = ThemeDesign(metrics(1.03f,mapOf("screenPadding" to 20f,"contentGap" to 12f,"sectionGap" to 18f,"statCardHeight" to 112f,"navHeight" to 80f,"homeHeroHeight" to 68f,"homeReviewHeight" to 68f,"homeCtaHeight" to 46f,"homeBottomGap" to 10f,"reviewHeaderHeight" to 96f,"cardElevationBase" to 5f,"dividerAlpha" to .72f,"reviewSelectedAlpha" to .16f,"cardBorderAlpha" to .55f,"cardBorderStrongAlpha" to .85f,"accentSurfaceAlpha" to .16f,"hierarchyBoost" to 1.08f)),ButtonStyle.FILLED,NavigationStyle.PILL,StatsLayoutStrategy.GRID_4_COLUMNS,ReviewsLayoutStrategy.HORIZONTAL_CARDS,LibraryLayoutStrategy.GRID_2_COLUMNS,ReviewPresentation.SWIPE_STACK,true,0xFF1A140A,0xFF0F1419)
        fun claud() = ThemeDesign(metrics(1.05f,mapOf("screenPadding" to 24f,"contentGap" to 16f,"sectionGap" to 24f,"statCardHeight" to 112f,"navHeight" to 78f,"reviewHeaderHeight" to 100f,"cardElevationBase" to 2f,"dividerAlpha" to .45f,"reviewSelectedAlpha" to .08f,"cardBorderAlpha" to .25f,"cardBorderStrongAlpha" to .45f,"accentSurfaceAlpha" to .08f)),ButtonStyle.OUTLINED,NavigationStyle.STANDARD,StatsLayoutStrategy.VERTICAL_LIST,ReviewsLayoutStrategy.VERTICAL_ROWS,LibraryLayoutStrategy.EXPANDED_LIST,ReviewPresentation.FLIP_FULLSCREEN,false)
        fun spark() = ThemeDesign(metrics(0.96f,mapOf("sectionGap" to 18f,"statCardHeight" to 126f,"navHeight" to 74f,"cardElevationBase" to 3f,"accentSurfaceAlpha" to .12f,"hierarchyBoost" to 1.06f)),ButtonStyle.FILLED,NavigationStyle.STANDARD,StatsLayoutStrategy.GRID_2X2,ReviewsLayoutStrategy.VERTICAL_ROWS,LibraryLayoutStrategy.GRID_2_COLUMNS,ReviewPresentation.STANDARD,true)
        fun gtp() = ThemeDesign(metrics(.88f,mapOf("screenPadding" to 16f,"contentGap" to 10f,"sectionGap" to 14f,"statCardHeight" to 118f,"navHeight" to 70f,"cardElevationBase" to 5f,"cardBorderAlpha" to .70f,"cardBorderStrongAlpha" to .95f,"accentSurfaceAlpha" to .18f,"hierarchyBoost" to 1.12f)),ButtonStyle.FILLED,NavigationStyle.COMPACT,StatsLayoutStrategy.GRID_4_COLUMNS,ReviewsLayoutStrategy.COMPACT_LIST,LibraryLayoutStrategy.GRID_2_COLUMNS,ReviewPresentation.SWIPE_STACK,true, iconStyle=IconStyle.FILLED, activeIconStyle=IconStyle.FILLED, iconSizeScale=.98f, activeIconSizeScale=1.04f, navIndicatorAlpha=.18f)
        fun default() = ThemeDesign(metrics(1f))
    }
}
enum class ButtonStyle { FILLED, OUTLINED }
enum class NavigationStyle { STANDARD, COMPACT, PILL }
enum class StatsLayoutStrategy { GRID_2X2, GRID_4_COLUMNS, VERTICAL_LIST, HORIZONTAL_ROW }
enum class ReviewsLayoutStrategy { VERTICAL_ROWS, HORIZONTAL_CARDS, COMPACT_LIST }
enum class LibraryLayoutStrategy { GRID_2_COLUMNS, EXPANDED_LIST }
enum class ReviewPresentation { STANDARD, SWIPE_STACK, FLIP_FULLSCREEN }

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
    val spacingScale: Float = 1f,
    val design: ThemeDesign = when (id.lowercase()) { "grok" -> ThemeDesign.grok(); "claud","claude" -> ThemeDesign.claud(); "spark" -> ThemeDesign.spark(); "gtp" -> ThemeDesign.gtp(); else -> ThemeDesign.default() }
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
            cornerSmall=10f, cornerMedium=14f, cornerLarge=20f, typographyScale=1f, densityScale=1f, spacingScale=1f, design=ThemeDesign.default()
        )

        val GROK = FlashLearnThemeSpec(
            id = "grok", name = "گروک",
            lightPrimary = 0xFFC79B32, darkPrimary = 0xFFE0B44C,
            lightSecondary = 0xFF9F7925, darkSecondary = 0xFFF0C65A,
            lightBackground = 0xFFF7F2E8, darkBackground = 0xFF080D13,
            lightSurface = 0xFFFFFCF5, darkSurface = 0xFF111820,
            lightSurfaceVariant = 0xFFF0E8D8, darkSurfaceVariant = 0xFF18222D,
            lightOnSurface = 0xFF17130C, darkOnSurface = 0xFFF7F0E3,
            lightOnSurfaceVariant = 0xFF756A59, darkOnSurfaceVariant = 0xFFB9B2A6,
            lightCard = 0xFFFFFBF2, darkCard = 0xFF121B24,
            lightOutline = 0xFFD8C08A, darkOutline = 0xFF6A5833,
            gradientStart = 0xFFC79B32, gradientEnd = 0xFFE0B44C,
            iconStyle = "outlined", elevationScale = 1.35f,
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

        private fun safeColor(v:String, fallback:Long):Long = runCatching { parseColor(v) }.getOrDefault(fallback)

        private fun parseColor(v:String):Long{
            val x=v.removePrefix("#")
            return (if(x.length==6)"FF$x" else x).toLong(16)
        }
        private fun hex(v:Long):String="#" + v.toString(16).padStart(8,'0')
    }
}

internal fun Long.asComposeColor()=Color(this)
