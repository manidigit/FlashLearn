plugins {
    id("com.android.application") version "8.2.2" apply false
    id("com.android.library") version "8.2.2" apply false
    id("org.jetbrains.kotlin.android") version "1.9.20" apply false
    id("org.jetbrains.kotlin.kapt") version "1.9.20" apply false
    id("com.google.dagger.hilt.android") version "2.51.1" apply false
}

// Temporary CI diagnostic: bypass KAPT stub generation so the Kotlin compiler
// can expose the underlying source error hidden by "Could not load module".
subprojects {
    tasks.matching { it.name.contains("kaptGenerateStubs") }.configureEach {
        enabled = false
    }
}
