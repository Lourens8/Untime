Build and deploy the app to the connected device.

## Build
```bash
cd /c/projects/MeetingAlarm
JAVA_HOME="/c/Program Files/Android/Android Studio/jbr" ./gradlew assembleDebug
```

## Install
ADB is at `/c/Users/loure/AppData/Local/Android/Sdk/platform-tools/adb.exe`.
Device connects wirelessly at `192.168.0.50:5555`.

```bash
export PATH="$PATH:/c/Users/loure/AppData/Local/Android/Sdk/platform-tools"
adb -s 192.168.0.50:5555 install -r /c/projects/MeetingAlarm/app/build/outputs/apk/debug/app-debug.apk
```

## Notes
- APK output: `app/build/outputs/apk/debug/app-debug.apk`
- Device IP may change; run `adb devices` to verify
