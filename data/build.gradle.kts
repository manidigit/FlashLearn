plugins { id("com.android.library"); id("org.jetbrains.kotlin.android"); id("org.jetbrains.kotlin.kapt") }
android {
    namespace = "com.flashlearn.data"; compileSdk = 34; defaultConfig { minSdk = 26; testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner" }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
    kotlinOptions { jvmTarget = "17" }
}
dependencies { implementation(project(":domain")); implementation(project(":database")); implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3"); implementation("javax.inject:javax.inject:1"); implementation("com.google.dagger:hilt-android:2.51.1"); kapt("com.google.dagger:hilt-compiler:2.51.1"); androidTestImplementation("androidx.test:runner:1.6.1"); androidTestImplementation("androidx.test.ext:junit:1.2.1"); androidTestImplementation("androidx.test:core:1.6.1"); androidTestImplementation("androidx.room:room-testing:2.6.1"); androidTestImplementation("androidx.test:rules:1.6.1") }
