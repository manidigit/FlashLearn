plugins { id("com.android.library");  }
android {
    namespace = "com.flashlearn.domain"; compileSdk = 34; defaultConfig { minSdk = 26 }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
    kotlinOptions { jvmTarget = "17" }
}
dependencies { implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3"); implementation("javax.inject:javax.inject:1"); testImplementation("junit:junit:4.13.2") }
