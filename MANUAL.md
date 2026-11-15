# Secure Communication lab — run steps

This lab has a single branch; there is no challenge/solution pair to switch
between.

## 1. Prerequisites

- Android Studio Rabbit (2026.2) or newer, with Android SDK platform 37 and
  build-tools 37.0.0
- JDK 17 or newer; Android Studio's bundled JBR works
- A device or emulator running Android 11 (API 30) or newer
- Network access to the training server

The app targets Android 15 (API 35) and compiles against API 37.

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

- The installed application ID is `defdev.labdroid.securecommunication`.
- The earlier `dev.def.lab.droid.securecommunication` build is a separate
  installed app; select **Secure Communication Lab** from the current build.
- Android Studio's bundled JBR can be selected as the Gradle JDK; shell
  builds can select it through `JAVA_HOME`.
