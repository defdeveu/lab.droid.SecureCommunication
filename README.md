# Secure Communication

Android teaching app for the "Development security in Android" course. It
encrypts and signs a message, sends it to the shared training server, and shows
the raw response together with its decrypted acknowledgment.

## Requirements

- Android Studio Rabbit (2026.2) or newer
- JDK 17 or newer (Android Studio's bundled JBR works)
- Android SDK platform 37 and build-tools 37.0.0
- A device or emulator running Android 7.0 (API 24) or newer
- Network access to the training server

## Build and install

```sh
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## Endpoint

The app posts to the hosted training server by default. A machine-local
override goes in `lab.local.properties`, which is gitignored;
`lab.local.properties.example` shows the key.

## Tests

```sh
./gradlew testDebugUnitTest
```

With a HiPinSeco server listening on `http://localhost:8090`, the live interop
check runs as well:

```sh
SECURE_COMMUNICATION_INTEROP=1 ./gradlew testDebugUnitTest --tests '*ServerInteropTest*'
```
