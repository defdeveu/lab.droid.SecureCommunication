# Secure Communication lab — run steps

This lab has a single branch; there is no challenge/solution pair to switch
between.

## 1. Prerequisites

- Android Studio Rabbit 2 (2026.2.2) or newer, with Android SDK platform 37 and
  build-tools 37.0.0
- JetBrains Runtime 25 from Android Studio's bundled JBR, selected through
  `gradle/gradle-daemon-jvm.properties`; no separate JDK installation needed
- A device or emulator running Android 11 (API 30) or newer
- Network access to the training server

The app targets and compiles against Android 17 (API 37), with Java/Kotlin
compilation target 25. For a shell build, select your installed bundled JBR
through `JAVA_HOME` if needed.

For Android Studio Quail 4, change only your local checkout's `agp` value in
`gradle/libs.versions.toml` from `9.5.1` to `9.4.1` before sync, and use
JetBrains Runtime 25. Leave that compatibility edit uncommitted. The default
AGP 9.5.1 configuration requires Rabbit 2 or newer.

## 2. Build and install

```sh
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## 3. Run

1. Launch **Secure Communication Lab**.
2. Type a message and press **Encrypt and send**.
3. Watch the status line, then read the **Encrypted wire response** pane and the
   **Decrypted acknowledgment** pane.
4. Press **Cancel** while a request is in flight to abort it.

## 4. Pointing at a local server

The supplied `lab.properties` already selects the course server; no local
file is required. Only when the trainer requests another endpoint, create
ignored `lab.local.properties` with the trainer's supplied `lab.endpoint`
value. Sync, rebuild and reinstall after changing it. Remove the override
and rebuild to return to the supplied course configuration.

Follow the trainer's routing and certificate-setup instructions. An endpoint
override does not configure a server, port forwarding or device trust.

## 5. Notes

- The installed application ID is `defdev.labs.droid.securecommunication`.
- Project daemon JVM criteria keep Studio and CLI builds on JetBrains Runtime 25.
