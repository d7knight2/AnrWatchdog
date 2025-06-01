# anrwatchdog Library

This is the ANR Watchdog library module. It provides ANR (Application Not Responding) detection for Android apps.

## Usage

Add this module as a dependency in your Android app module:

```
dependencies {
    implementation(project(":anrwatchdog"))
}
```

Initialize the watchdog in your Application class (see demoapp for example):

```kotlin
import com.example.anrwatchdog.ANRWatchdog

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        ANRWatchdog.initialize(this)
            .setLogLevel(Log.DEBUG)
            .start()
    }
}
```

## Build Tasks

To build the library:

```
./gradlew :anrwatchdog:assembleDebug
```

---
