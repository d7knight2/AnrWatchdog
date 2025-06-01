plugins {
    id("com.android.library")
    kotlin("android")
}

android {
    namespace = "com.example.anrwatchdog"
    compileSdk = 34

    defaultConfig {
        minSdk = 21
        targetSdk = 34
    }
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
}
