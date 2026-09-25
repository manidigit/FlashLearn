plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.flashlearn.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.flashlearn.app"
        minSdk = 26
        targetSdk = 34
        versionCode = 625
        versionName = "6.25"
        
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        buildConfigField("String", "VERSION_CODE_NAME", "\"625\"")
        buildConfigField("String", "BUILD_TYPE", "\"${buildTypes.getByName("release").name}\"")
        buildConfigField("String", "BUILD_DATE", "\"2026-09-25\"")
        buildConfigField("String", "THEME_VERSION", "\"2.0-Complete\"")
        buildConfigField("String", "THEME_STATUS", "\"۳ Screens Fixed, 8 Patterns Ready\"")
        buildConfigField("String", "APP_GITHUB_URL", "\"https://github.com/manidigit/FlashLearn\"")
        buildConfigField("String", "APP_AUTHOR", "\"Mani\"")
        buildConfigField("String", "APP_LANGUAGE", "\"English / Persian\"")
        buildConfigField("String", "APP_DATABASE", "\"Room\"")
        buildConfigField("String", "APP_AI_ASSISTANT", "\"ChatGPT\"")
        buildConfigField("String", "APP_BUILD_DATE", "\"2026-09-25\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.4"
    }
}

dependencies {
    implementation(project(":domain"))
    implementation(project(":data"))
    implementation(project(":database"))

    // Androidx
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.activity:activity-compose:1.8.0")
    
    // Jetpack Compose
    implementation("androidx.compose.ui:ui:1.5.4")
    implementation("androidx.compose.ui:ui-graphics:1.5.4")
    implementation("androidx.compose.ui:ui-tooling-preview:1.5.4")
    implementation("androidx.compose.material3:material3:1.2.1")
    implementation("androidx.compose.material:material-icons-extended:1.5.4")
    
    // Room
    implementation("androidx.room:room-runtime:2.6.0")
    kapt("androidx.room:room-compiler:2.6.0")
    implementation("androidx.room:room-ktx:2.6.0")
    
    // Hilt
    implementation("com.google.dagger:hilt-android:2.51.1")
    kapt("com.google.dagger:hilt-compiler:2.51.1")
    
    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}

