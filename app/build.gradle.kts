import java.time.LocalDate

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.kapt")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.flashlearn.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.flashlearn.app"
        minSdk = 26
        targetSdk = 34
        versionCode = 114
        versionName = "6.14"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "APP_AUTHOR", "\"FlashLearn\"")
        buildConfigField("String", "APP_LANGUAGE", "\"Kotlin\"")
        buildConfigField("String", "APP_DATABASE", "\"Room\"")
        buildConfigField("String", "APP_AI_ASSISTANT", "\"AI-assisted development\"")
        buildConfigField("String", "APP_GITHUB_URL", "\"https://github.com/manidigit/FlashLearn\"")
        buildConfigField("String", "APP_BUILD_DATE", "\"${LocalDate.now()}\"")
    }

    signingConfigs {
        getByName("debug") {
            storeFile = file("../keystore/debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "androiddebugkey"
        }
        create("release") {
            val storeFilePath = System.getenv("FL_RELEASE_STORE_FILE")
            val storePasswordValue = System.getenv("FL_RELEASE_STORE_PASSWORD")
            val keyAliasValue = System.getenv("FL_RELEASE_KEY_ALIAS")
            val keyPasswordValue = System.getenv("FL_RELEASE_KEY_PASSWORD")
            if (!storeFilePath.isNullOrBlank()) storeFile = file(storeFilePath)
            if (!storePasswordValue.isNullOrBlank()) storePassword = storePasswordValue
            if (!keyAliasValue.isNullOrBlank()) keyAlias = keyAliasValue
            if (!keyPasswordValue.isNullOrBlank()) keyPassword = keyPasswordValue
        }
    }

    buildTypes {
        getByName("debug") { signingConfig = signingConfigs.getByName("debug") }
        getByName("release") { signingConfig = signingConfigs.getByName("release"); isMinifyEnabled = false }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions { kotlinCompilerExtensionVersion = "1.5.4" }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
    kotlinOptions { jvmTarget = "17" }
}

dependencies {
    implementation(project(":domain")); implementation(project(":data")); implementation(project(":database")); implementation(project(":core"))
    implementation("com.google.dagger:hilt-android:2.51.1")
    kapt("com.google.dagger:hilt-compiler:2.51.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation(platform("androidx.compose:compose-bom:2024.02.00"))
    implementation("androidx.activity:activity-compose:1.9.2")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.02.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    androidTestImplementation("androidx.test:runner:1.6.1")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test:core:1.6.1")
    testImplementation("junit:junit:4.13.2")
}