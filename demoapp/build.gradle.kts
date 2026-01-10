plugins {
    id("com.android.application")
    kotlin("android")
    id("com.google.gms.google-services")
    id("com.google.firebase.appdistribution")
}

android {
    namespace = "com.example.demoapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.demoapp"
        minSdk = 21
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
}

// Firebase App Distribution configuration
configure<com.google.firebase.appdistribution.gradle.AppDistributionExtension> {
    releaseNotesFile = file("release-notes.txt").path
    groups = "testers"
    serviceCredentialsFile = System.getenv("FIREBASE_SERVICE_CREDENTIALS") ?: ""
}

dependencies {
    implementation(project(":anrwatchdog"))
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.fragment:fragment-ktx:1.8.9")
    debugImplementation("com.squareup.leakcanary:leakcanary-android:2.14")
    
    // Firebase dependencies
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    implementation("com.google.firebase:firebase-analytics")
    
    // Testing dependencies
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.test:runner:1.5.2")
    androidTestImplementation("androidx.test:rules:1.5.0")
    androidTestImplementation("org.jetbrains.kotlin:kotlin-test:1.9.0")
}
