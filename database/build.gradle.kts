plugins { id("com.android.library"); id("org.jetbrains.kotlin.kapt") }
android {
    namespace = "com.flashlearn.database"; compileSdk = 37; defaultConfig { minSdk = 26; testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner" }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
    kotlinOptions { jvmTarget = "17" }
}
kapt {
    arguments {
        arg("room.schemaLocation", "$projectDir/schemas")
    }
}
dependencies { implementation(project(":domain")); api("androidx.room:room-runtime:2.6.1"); api("androidx.room:room-ktx:2.6.1"); kapt("androidx.room:room-compiler:2.6.1"); androidTestImplementation("androidx.test:runner:1.6.1"); androidTestImplementation("androidx.test.ext:junit:1.2.1"); androidTestImplementation("androidx.room:room-testing:2.6.1"); androidTestImplementation("androidx.test:core:1.6.1"); testImplementation("junit:junit:4.13.2") }
