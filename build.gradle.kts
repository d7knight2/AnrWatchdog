buildscript {
    val firebaseCredentialsPath = System.getenv("FIREBASE_SERVICE_CREDENTIALS")
    val hasFirebaseCredentials = !firebaseCredentialsPath.isNullOrBlank()

    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.2.2")
        classpath(kotlin("gradle-plugin", version = "1.9.0"))
        classpath("com.google.gms:google-services:4.4.0")

        if (hasFirebaseCredentials) {
            classpath("com.google.firebase:firebase-appdistribution-gradle:4.0.1")
        }
    }
}
