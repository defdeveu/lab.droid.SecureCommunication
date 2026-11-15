# Secure Communication

Android teaching app for the "Development security in Android" course. It
encrypts and signs a message, sends it to the shared training server, and shows
the raw response together with its decrypted acknowledgment.

## Requirements

- Android Studio Rabbit (2026.2) or newer
- JDK 17 or newer (Android Studio's bundled JBR works)
- Android SDK platform 37 and build-tools 37.0.0
- A device or emulator running Android 11 (API 30) or newer
- Network access to the training server

The app targets Android 15 (API 35).

## Build and install

```sh
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## Endpoint

The course endpoint is configured in the supplied `lab.properties`. Normal
course use needs no local configuration file or endpoint setup.

## Tests

```sh
./gradlew testDebugUnitTest
```

With a HiPinSeco server listening on `http://localhost:8090`, the live interop
check runs as well:

```sh
SECURE_COMMUNICATION_INTEROP=1 ./gradlew testDebugUnitTest --tests '*ServerInteropTest*'
```

## Font and icon licenses

Bundled variable fonts and their licenses are listed in
`app/src/main/assets/licenses/fonts.txt`. The Shield Moon icon uses Material
Symbols Rounded under `app/src/main/assets/licenses/material-symbols.LICENSE.txt`.
