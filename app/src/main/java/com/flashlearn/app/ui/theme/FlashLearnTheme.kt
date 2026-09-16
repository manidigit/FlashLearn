package com.flashlearn.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.flashlearn.app.ui.AppearanceMode

@Composable
fun FlashLearnTheme(appearance:AppearanceMode=AppearanceMode.SYSTEM,themeId:String="modern_purple",content:@Composable()->Unit){
 val context=LocalContext.current
 val spec=FlashLearnThemeSpec.BUILT_IN.firstOrNull{it.id==themeId}?:FlashLearnThemeSpec.loadCustom(context).firstOrNull{it.id==themeId}?:FlashLearnThemeSpec.MODERN_PURPLE
 val dark=when(appearance){AppearanceMode.SYSTEM->isSystemInDarkTheme();AppearanceMode.LIGHT->false;AppearanceMode.DARK->true}
 val colors=if(dark)darkColorScheme(primary=Color(spec.darkPrimary),onPrimary=Color.White,secondary=Color(spec.darkSecondary),background=Color(spec.darkBackground),surface=Color(spec.darkSurface),surfaceVariant=Color(spec.darkSurfaceVariant),onSurface=Color(spec.darkOnSurface),onSurfaceVariant=Color(spec.darkOnSurfaceVariant))else lightColorScheme(primary=Color(spec.lightPrimary),onPrimary=Color.White,secondary=Color(spec.lightSecondary),background=Color(spec.lightBackground),surface=Color(spec.lightSurface),surfaceVariant=Color(spec.lightSurfaceVariant),onSurface=Color(spec.lightOnSurface),onSurfaceVariant=Color(spec.lightOnSurfaceVariant))
 val base=Typography();val s=spec.typographyScale
 fun androidx.compose.ui.text.TextStyle.scaled()=copy(fontSize=fontSize*s)
 MaterialTheme(colorScheme=colors,typography=base.copy(displayLarge=base.displayLarge.scaled(),displayMedium=base.displayMedium.scaled(),displaySmall=base.displaySmall.scaled(),headlineLarge=base.headlineLarge.scaled().copy(fontWeight=androidx.compose.ui.text.font.FontWeight.Bold),headlineMedium=base.headlineMedium.scaled().copy(fontWeight=androidx.compose.ui.text.font.FontWeight.Bold),headlineSmall=base.headlineSmall.scaled(),titleLarge=base.titleLarge.scaled().copy(fontWeight=androidx.compose.ui.text.font.FontWeight.Bold),titleMedium=base.titleMedium.scaled(),titleSmall=base.titleSmall.scaled(),bodyLarge=base.bodyLarge.scaled(),bodyMedium=base.bodyMedium.scaled(),bodySmall=base.bodySmall.scaled(),labelLarge=base.labelLarge.scaled(),labelMedium=base.labelMedium.scaled(),labelSmall=base.labelSmall.scaled()),shapes=Shapes(extraSmall=RoundedCornerShape(spec.cornerSmall.dp),small=RoundedCornerShape(spec.cornerSmall.dp),medium=RoundedCornerShape(spec.cornerMedium.dp),large=RoundedCornerShape(spec.cornerLarge.dp)),content=content)
}
