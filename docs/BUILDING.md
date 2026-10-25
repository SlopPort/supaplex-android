# Build the Android APK

These instructions are for developers. To play, use the [installation guide](../README.md#install-and-start-playing).

## Required tools

Install Git and Android Studio with its Android SDK tools. In SDK Manager's **SDK Tools** tab, enable **Show Package Details** to install the pinned NDK and CMake versions.

| Component | Version |
| --- | --- |
| JDK | 17 minimum; JDK 21 used locally and in the CI template |
| Android SDK Platform | 35 |
| Android SDK Build-Tools | 35.0.0 |
| Android SDK Platform-Tools | Provides `adb` for optional device tests/installing |
| NDK (side by side) | 27.2.12479018 |
| CMake (SDK Manager package) | 3.22.1 |
| Gradle | 8.10.2, downloaded by the included wrapper |
| Android Gradle Plugin | 8.8.0, resolved by Gradle |

The first checkout/build needs internet to fetch dependencies and tools. The finished game does not. The local workflow was tested on Windows; Linux/macOS commands are provided without a claim of testing those host systems.

## 1. Get the source and dependencies

Run these commands from a folder where you keep projects:

```sh
git clone https://github.com/SlopPort/supaplex-android.git
cd supaplex-android
git submodule update --init vendor/SDL vendor/SDL_mixer
git -C vendor/SDL_mixer submodule update --init external/libxmp
```

These targeted submodule commands fetch only the dependencies needed by this build. Git records their exact commits; do not substitute SDL3 or an arbitrary newer branch. See the [dependency record](../evidence/source-commits.json).

GitHub's automatic **Source code** ZIP does not contain submodule contents. The release attachment `Supaplex-Offline-1.3-sources.zip` includes them. If using that attachment, extract it, open a terminal in its `supaplex-android` folder, and continue at step 2; no submodule commands are needed.

## 2. Locate your SDK and JDK

Create `android/local.properties` with the actual SDK directory. Do not commit this file. Windows example using forward slashes (replace the example with your path):

```properties
sdk.dir=C:/Android/Sdk
```

Linux example:

```properties
sdk.dir=/opt/android-sdk
```

Android Studio can create this file when you open the `android` folder and choose an SDK. For terminal builds, set `JAVA_HOME` to the JDK directory, or ensure the intended `java` is on `PATH`. Check `java -version`. For IDE builds, choose the JDK in Android Studio's Gradle settings. A separate Gradle installation is not needed.

## 3. Build and check

From the repository root, in Windows PowerShell:

```powershell
cd android
.\gradlew.bat assembleDebug assembleDebugAndroidTest lintDebug
```

From the repository root, on Linux/macOS:

```sh
cd android
./gradlew assembleDebug assembleDebugAndroidTest lintDebug
```

Wait for `BUILD SUCCESSFUL`. The playable APK is here, relative to the repository root:

```text
android/app/build/outputs/apk/debug/app-debug.apk
```

It contains ARMv7, ARM64, x86, and x86_64 libraries. Minimum SDK is 21; target/compile SDK is 35. Native libraries and APK packaging use 16 KB alignment. These settings do not establish compatibility with every phone.

`assembleDebugAndroidTest` builds a separate test APK; it does not run phone tests. Follow the [device-test guide](VALIDATION.md#run-the-interface-tests-on-a-device) to run them. Lint exclusions are limited to optional SDL platform code and explained in `android/app/lint.xml`.

## 4. Install on a device (optional)

Enable Developer options and USB debugging, connect the phone, and authorize the computer when prompted. In a new terminal at the repository root:

```sh
adb devices
```

Replace `DEVICE_ID` with the intended phone's ID from that list. It is a placeholder, not a literal device name. Always select the intended phone explicitly when multiple devices are attached.

```sh
adb -s DEVICE_ID install -r android/app/build/outputs/apk/debug/app-debug.apk
adb -s DEVICE_ID shell am start -n pl.offline.supaplex/.GameActivity
```

`-r` preserves app data when Android accepts the update's signature. A different debug key prevents updating an existing installation; read [save-data precautions](ANDROID.md#updates-and-save-data). Do not clear app data during normal builds/tests.

## CI and release signing

An optional GitHub Actions template is provided at `ci/android-apk.yml.example`. It builds the APK, test APK, and lint report. It is not enabled or CI-validated in this repository. To enable it on your own repository, copy it to `.github/workflows/android.yml` and push using credentials authorized to edit workflows. Device tests are manual; the template does not run them. Upstream workflows are archived in `docs/upstream/workflows`.

Local and CI debug APKs may have different signatures. CI artifacts are test builds, not a guaranteed update channel for the downloadable APK. A stable release-signing key is needed for a long-term update channel. No signing keys are included in this repository.
