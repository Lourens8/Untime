# Building MeetingAlarm

## Prerequisites
- Android SDK installed at `C:\Users\loure\AppData\Local\Android\Sdk` (configured in `local.properties`)
- Java is provided by Android Studio's bundled JBR at `C:\Program Files\Android\Android Studio\jbr`

## Command-line build

From the project root (`C:\projects\MeetingAlarm`):

```bash
# Set JAVA_HOME to Android Studio's bundled JDK
export JAVA_HOME="/c/Program Files/Android/Android Studio/jbr"

# Debug build
./gradlew assembleDebug

# Release build
./gradlew assembleRelease
```

The debug APK is output to:
```
app/build/outputs/apk/debug/app-debug.apk
```

## Notes
- The Gradle wrapper (`gradlew`, `gradlew.bat`, `gradle/wrapper/`) is included in the project
- Gradle version: 8.5
- AGP version: 8.2.2
- Kotlin version: 1.9.22
- No system Gradle or Java installation is required — Android Studio's bundled JBR is used
- Launcher icon mipmap resources are not yet generated; use Android Studio's **Image Asset** wizard (`res` → New → Image Asset) to create them before release builds
