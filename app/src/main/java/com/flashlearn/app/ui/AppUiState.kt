package com.flashlearn.app.ui

import com.flashlearn.app.navigation.AppRoutes
import com.flashlearn.domain.model.QuizDifficulty
import com.flashlearn.domain.model.VocabularyDifficulty
import com.flashlearn.domain.settings.SettingsKeys

enum class AppearanceMode { SYSTEM, LIGHT, DARK }
enum class AppLayoutDirection { RTL, LTR }
enum class AccentColor { PURPLE, BLUE, GREEN, ORANGE, PINK }
enum class LearningLanguage(val code:String,val labelFa:String,val flag:String){PERSIAN("fa","فارسی","🇮🇷"),SPANISH("es","اسپانیایی","🇪🇸"),ENGLISH("en","انگلیسی","🇬🇧")}
data class LanguagePair(val source:LearningLanguage=LearningLanguage.SPANISH,val target:LearningLanguage=LearningLanguage.PERSIAN){init{require(source!=target)};fun reversed()=LanguagePair(target,source)}
data class AppUiState(
 val selectedRoute:String=AppRoutes.HOME,val selectedConceptId:java.util.UUID?=null,val appearance:AppearanceMode=AppearanceMode.SYSTEM,
 val accentColor:AccentColor=AccentColor.PURPLE,val themeId:String="modern_purple",val layoutDirection:AppLayoutDirection=AppLayoutDirection.RTL,
 val languagePair:LanguagePair=LanguagePair(),val personalWordDifficulty:VocabularyDifficulty?=null,val quizDifficulty:QuizDifficulty=QuizDifficulty.MEDIUM,
 val difficultyThreshold:Int=SettingsKeys.DEFAULT_THRESHOLD_DIFFICULTY,val maximumReviewCards:Int=SettingsKeys.DEFAULT_MAXIMUM_REVIEW_CARDS){init{require(selectedRoute in AppRoutes.all());require(difficultyThreshold in 1..20);require(maximumReviewCards in SettingsKeys.MINIMUM_REVIEW_CARDS..SettingsKeys.MAXIMUM_REVIEW_CARDS_LIMIT)}}
