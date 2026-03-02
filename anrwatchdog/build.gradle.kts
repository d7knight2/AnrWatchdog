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
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
    
    // Test dependencies
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito:mockito-core:5.3.1")
    testImplementation("org.mockito:mockito-inline:5.2.0")
    testImplementation("org.jetbrains.kotlin:kotlin-test:1.9.0")
}
