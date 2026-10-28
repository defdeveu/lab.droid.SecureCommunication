# Secure Communication lab — run steps

This lab has a single branch; there is no challenge/solution pair to switch
between.

## 1. Prerequisites

- Android Studio Rabbit (2026.2) or newer, with Android SDK platform 37 and
  build-tools 37.0.0
- JDK 17 or newer; Android Studio's bundled JBR works
- A device or emulator running Android 7.0 (API 24) or newer
- Network access to the training server

## 2. Build and install

```sh
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## 3. Run

1. Launch **Secure Communication**.
2. Type a message and press **Encrypt and send**.
3. Watch the status line, then read the **Server response** pane and the
   **Decrypted acknowledgment** pane.
4. Press **Cancel** while a request is in flight to abort it.

## 4. Pointing at a local server

The local edge serves a certificate for `localhost`, so keep that name and bridge
the port instead of re-addressing the endpoint:

```sh
adb reverse tcp:8443 tcp:8443
```

With the bridge in place, copy `lab.local.properties.example` to
`lab.local.properties` and set:

```properties
lab.endpoint=https://localhost:8443/secure-communication/request
```

Rebuild after changing the override, because it is read at build time. The
bridge forwards the device or emulator's `localhost:8443` to the machine
running `adb`, so the server must be listening there; when the server runs in a
virtualised guest, forward its port to that machine first.

The app trusts system CAs only, while a debug build's network security config
also admits user-installed CAs, so install the edge's root certificate on the
device or emulator (Settings → Security → Encryption & credentials → Install a
certificate → CA certificate) and the request can succeed.

## 5. Notes

- A local HTTPS server needs its root certificate trusted as described above.
- On a physical device, bridge the port the same way with `adb reverse`; the
  emulator's host alias `10.0.2.2` is not usable here, because the certificate
  is issued for `localhost`.

## 6. Validated emulator setup

The instrumented tests and the runtime rows were run against this setup:

| Item | Value |
| --- | --- |
| System image | Google Play arm64 image for API 36, product `sdk_gphone64_arm64` (Play Store present) |
| API / extension level | 36 (Android 16) / SDK extensions 17 |
| ABI | arm64-v8a |
| Build | `BE2A.250530.026.D1` |
| Emulator | reachable over adb as `emulator-5554`; the emulator console is not exposed on this connection, so the AVD name and the emulator build are not readable from here |

This workspace's SDK also carries the API 37 Play arm64 image
(`system-images;android-37.0;google_apis_playstore;arm64-v8a`) and the local
emulator binary 37.2.12.0, which is the intended target once a local emulator
can run.
