plugins {
    id("com.android.library")
    kotlin("android")
    jacoco
}

android {
    namespace = "com.example.anrwatchdog"
    compileSdk = 34

    defaultConfig {
        minSdk = 21
        targetSdk = 34
    }
    
    buildTypes {
        debug {
            enableUnitTestCoverage = true
            enableAndroidTestCoverage = true
        }
    }
    
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    
    // Test dependencies
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito:mockito-core:5.3.1")
    testImplementation("org.mockito:mockito-inline:5.2.0")
    testImplementation("org.jetbrains.kotlin:kotlin-test:1.9.0")
}

// JaCoCo configuration for Android library
tasks.register<JacocoReport>("jacocoTestReport") {
    dependsOn("testDebugUnitTest")
    
    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }
    
    sourceDirectories.setFrom(files("${project.projectDir}/src/main/kotlin"))
    classDirectories.setFrom(files("${project.buildDir}/tmp/kotlin-classes/debug"))
    executionData.setFrom(files("${project.buildDir}/jacoco/testDebugUnitTest.exec"))
}

