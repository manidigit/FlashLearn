package com.flashlearn.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import com.flashlearn.app.ui.AppearanceMode

@Composable
fun FlashLearnTheme(appearance:AppearanceMode=AppearanceMode.SYSTEM,themeId:String="modern_purple",content:@Composable()->Unit){
 val spec=FlashLearnThemeSpec.BUILT_IN.firstOrNull{it.id==themeId}?:FlashLearnThemeSpec.MODERN_PURPLE
 val dark=when(appearance){AppearanceMode.SYSTEM->isSystemInDarkTheme();AppearanceMode.LIGHT->false;AppearanceMode.DARK->true}
 val colors=if(dark)darkColorScheme(primary=Color(spec.darkPrimary),onPrimary=Color.White,secondary=Color(spec.darkSecondary),background=Color(spec.darkBackground),surface=Color(spec.darkSurface),surfaceVariant=Color(spec.darkSurfaceVariant),onSurface=Color(spec.darkOnSurface),onSurfaceVariant=Color(spec.darkOnSurfaceVariant)) else lightColorScheme(primary=Color(spec.lightPrimary),onPrimary=Color.White,secondary=Color(spec.lightSecondary),background=Color(spec.lightBackground),surface=Color(spec.lightSurface),surfaceVariant=Color(spec.lightSurfaceVariant),onSurface=Color(spec.lightOnSurface),onSurfaceVariant=Color(spec.lightOnSurfaceVariant))
 val base=Typography();val t=spec.typographyScale
 MaterialTheme(colorScheme=colors,typography=base.copy(
  displayLarge=base.displayLarge.copy(fontSize=base.displayLarge.fontSize*t),displayMedium=base.displayMedium.copy(fontSize=base.displayMedium.fontSize*t),displaySmall=base.displaySmall.copy(fontSize=base.displaySmall.fontSize*t),
  headlineLarge=base.headlineLarge.copy(fontSize=base.headlineLarge.fontSize*t,fontWeight=androidx.compose.ui.text.font.FontWeight.Bold),headlineMedium=base.headlineMedium.copy(fontSize=base.headlineMedium.fontSize*t,fontWeight=androidx.compose.ui.text.font.FontWeight.Bold),headlineSmall=base.headlineSmall.copy(fontSize=base.headlineSmall.fontSize*t),
  titleLarge=base.titleLarge.copy(fontSize=base.titleLarge.fontSize*t,fontWeight=androidx.compose.ui.text.font.FontWeight.Bold),titleMedium=base.titleMedium.copy(fontSize=base.titleMedium.fontSize*t),titleSmall=base.titleSmall.copy(fontSize=base.titleSmall.fontSize*t),
  bodyLarge=base.bodyLarge.copy(fontSize=base.bodyLarge.fontSize*t),bodyMedium=base.bodyMedium.copy(fontSize=base.bodyMedium.fontSize*t),bodySmall=base.bodySmall.copy(fontSize=base.bodySmall.fontSize*t),labelLarge=base.labelLarge.copy(fontSize=base.labelLarge.fontSize*t),labelMedium=base.labelMedium.copy(fontSize=base.labelMedium.fontSize*t),labelSmall=base.labelSmall.copy(fontSize=base.labelSmall.fontSize*t)
 ),shapes=Shapes(extraSmall=RoundedCornerShape(spec.cornerSmall.sp()),small=RoundedCornerShape(spec.cornerSmall.sp()),medium=RoundedCornerShape(spec.cornerMedium.sp()),large=RoundedCornerShape(spec.cornerLarge.sp())),content=content)
}
private fun Float.sp()=this.sp
