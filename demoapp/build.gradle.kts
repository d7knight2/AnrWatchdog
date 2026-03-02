plugins {
    id("com.android.application")
    kotlin("android")
    id("com.google.gms.google-services")
}

val firebaseCredentialsPath = System.getenv("FIREBASE_SERVICE_CREDENTIALS")
val hasFirebaseCredentials = !firebaseCredentialsPath.isNullOrBlank() && file(firebaseCredentialsPath).exists()

if (hasFirebaseCredentials) {
    apply(plugin = "com.google.firebase.appdistribution")
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
if (hasFirebaseCredentials) {
    extensions.findByName("appDistribution")?.let { extension ->
        extension.javaClass.getMethod("setReleaseNotesFile", String::class.java)
            .invoke(extension, file("release-notes.txt").path)
        extension.javaClass.getMethod("setGroups", String::class.java)
            .invoke(extension, "testers")
        extension.javaClass.getMethod("setServiceCredentialsFile", String::class.java)
            .invoke(extension, firebaseCredentialsPath)
    }
}

dependencies {
    implementation(project(":anrwatchdog"))
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.fragment:fragment-ktx:1.6.2")
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
