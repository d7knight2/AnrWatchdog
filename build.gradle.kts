plugins {
    kotlin("jvm") version "1.9.0"
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-debug:1.7.3")
    implementation("io.reactivex.rxjava2:rxjava:2.2.21")
    implementation("io.reactivex.rxjava2:rxkotlin:2.4.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactive:1.7.3")
}

kotlin {
    jvmToolchain(8)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
        // Removed vendor-specific requirement to use the installed OpenJDK 8
    }
}

buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.2.2")
        classpath(kotlin("gradle-plugin", version = "1.9.0"))
        classpath("com.google.gms:google-services:4.4.0")
        classpath("com.google.firebase:firebase-appdistribution-gradle:4.0.1")
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}