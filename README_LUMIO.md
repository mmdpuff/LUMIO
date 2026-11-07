<div align="center">

<img src="assets/lumio-hero.gif" alt="LUMIO — animated neon Bluetooth and ESP32 lighting controller banner" width="100%" />

<br />

**A little light. A lot of atmosphere.**

Control an ESP32's onboard LED from an Android app built with Kotlin, Jetpack Compose, and Bluetooth Low Energy.

<br />

[![Android](https://img.shields.io/badge/Android-API%2024%2B-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com/)
[![Kotlin](https://img.shields.io/badge/Kotlin-Compose-A97BFF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Bluetooth](https://img.shields.io/badge/Bluetooth-Low%20Energy-3976F6?style=for-the-badge&logo=bluetooth&logoColor=white)](https://www.bluetooth.com/)
[![ESP32](https://img.shields.io/badge/Hardware-ESP32-FF7C8B?style=for-the-badge)](https://www.espressif.com/)

[**Explore Features**](#-features) &nbsp;•&nbsp; [**Get Started**](#-getting-started) &nbsp;•&nbsp; [**BLE Protocol**](#-ble-protocol) &nbsp;•&nbsp; [**Contribute**](#-contributing)

</div>

---

## ✨ Meet LUMIO

**LUMIO** is a sleek Android companion for experimenting with **Bluetooth Low Energy (BLE)** and **ESP32-based lighting**. It pairs a polished, dark-first interface with practical wireless hardware control: discover a compatible ESP32, establish a BLE connection, and toggle its onboard LED straight from your phone.

The interface also includes a beautifully styled color picker, live HEX/RGB readouts, and a six-color palette as a foundation for richer lighting experiences.

> [!IMPORTANT]
> **Current implementation:** the ESP32 onboard LED **ON/OFF** command is wired to BLE. The color picker and quick-color swatches are **UI features**; they do **not currently transmit RGB values to the ESP32**. LUMIO is a work in progress, and this README intentionally distinguishes implemented behavior from future ideas.

## ⚡ Features

<table>
<tr>
<td width="50%" valign="top">

### 🔗 BLE discovery

Scan for an ESP32 advertising as `Lumio-ESP32`, connect over Bluetooth LE, and discover its GATT service.

</td>
<td width="50%" valign="top">

### 💡 Onboard LED control

Turn the ESP32 onboard LED on and off from an Android interface using lightweight BLE write commands.

</td>
</tr>
<tr>
<td width="50%" valign="top">

### 🎨 Color playground

Explore an interactive color picker with live HEX and RGB values, plus six colorful swatches in the UI.

</td>
<td width="50%" valign="top">

### 🌌 Dark, refined UI

A clean Jetpack Compose / Material 3 experience with subtle accents, modern controls, and connection status.

</td>
</tr>
</table>

## 🧩 Tech Stack

| Layer | Technology |
|:--|:--|
| Platform | Android (minimum SDK 24) |
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Connectivity | Bluetooth Low Energy (BLE) / Android GATT APIs |
| Hardware target | ESP32 with compatible BLE firmware |
| Build system | Gradle Kotlin DSL |

## 🚀 Getting Started

### What you'll need

- **Android Studio** with a compatible Android SDK and Gradle toolchain
- An **Android phone with Bluetooth LE** (the project declares `minSdk = 24`)
- An **ESP32** running firmware that advertises the expected device name and GATT identifiers
- A USB cable or wireless debugging setup to run the Android app

### 1 — Clone the project

```bash
git clone https://github.com/mmdpuff/LUMIO.git
cd LUMIO
```

### 2 — Open in Android Studio

Open the cloned folder, allow Gradle to sync, and select the **app** run configuration.

### 3 — Build

**macOS / Linux**

```bash
./gradlew :app:assembleDebug
```

**Windows**

```powershell
.\gradlew.bat :app:assembleDebug
```

The debug APK is generated at `app/build/outputs/apk/debug/app-debug.apk`.

### 4 — Connect your ESP32

1. Flash or run **compatible firmware** on your ESP32 (firmware is **not included** in this repository).
2. Make sure the peripheral advertises the name **`Lumio-ESP32`** and exposes the service and characteristic shown below.
3. Enable Bluetooth on your Android phone, launch LUMIO, and grant the requested Bluetooth permissions.
4. Tap the connection control to scan for the ESP32. Once the connection is ready, use the **Onboard LED** power button.

> [!NOTE]
> The Android app expects a specific BLE peripheral. Having an ESP32 alone is not enough; its firmware must expose the matching GATT service, characteristic, and command handling.

## 📡 BLE Protocol

LUMIO uses a minimal, human-readable BLE command protocol.

| Property | Current value |
|:--|:--|
| Advertised device name | `Lumio-ESP32` |
| Service UUID | `12345678-1234-5678-1234-567812345678` |
| Characteristic UUID | `12345678-1234-5678-1234-567812345677` |
| LED ON command | UTF-8 text `1` |
| LED OFF command | UTF-8 text `0` |
| Write method | GATT characteristic write (with response) |
| Target LED | ESP32 onboard LED (GPIO 2 in the UI) |

```text
┌──────────────────────────┐      Bluetooth Low Energy      ┌──────────────────────────┐
│        LUMIO APP         │ ──────────────────────────────▶│           ESP32          │
│  Kotlin · Jetpack Compose│    GATT write: "1" / "0"       │     Compatible firmware  │
│     Connect / Toggle     │◀────────────────────────────── │      Onboard LED         │
└──────────────────────────┘        GATT callback           └──────────────────────────┘
```

The Android implementation scans for the exact advertised name, discovers the expected GATT characteristic, and writes UTF-8 commands to it. The current sender limits payloads to **1–20 bytes**.

## 🗺️ Project Structure

```text
LUMIO/
├── app/
│   ├── src/main/
│   │   ├── java/com/mmdpuff/lumio/  # App code and Compose UI
│   │   ├── AndroidManifest.xml       # Bluetooth permissions & activity
│   │   └── res/                      # Android resources
│   └── build.gradle.kts              # Android module configuration
├── gradle/                           # Gradle wrapper / version catalog
├── build.gradle.kts                  # Root build configuration
├── settings.gradle.kts               # Project settings
└── gradlew                           # Build wrapper
```

## 🛣️ Roadmap

The following are **ideas**, not claims about currently shipped functionality:

- [x] Compose-based dark interface
- [x] BLE scanning and ESP32 GATT connection
- [x] Onboard LED on/off command
- [x] Interactive color picker with HEX / RGB display
- [ ] Send selected RGB colors to compatible LED hardware
- [ ] Make quick-color swatches selectable and actionable
- [ ] Add reconnect and error-state polish
- [ ] Add hardware/firmware setup examples
- [ ] Add screenshots and a device demo

## 🤝 Contributing

Want to make LUMIO brighter? Contributions, ideas, and bug reports are welcome.

1. [Open an issue](https://github.com/mmdpuff/LUMIO/issues) to discuss a feature or report a bug.
2. Fork the repository and create a focused branch.
3. Make your changes and test with compatible ESP32 hardware when applicable.
4. Submit a pull request explaining what changed and how it was tested.

## 💜 Built with curiosity

LUMIO is a small project with a big obsession: making the connection between **beautiful software** and **physical hardware** feel effortless.

<div align="center">

**Make your space glow. Make your ideas real.**

[⭐ Star LUMIO](https://github.com/mmdpuff/LUMIO) &nbsp; · &nbsp; [🐛 Report an issue](https://github.com/mmdpuff/LUMIO/issues)

<sub>Crafted by <a href="https://github.com/mmdpuff">@mmdpuff</a> · Android × Kotlin × ESP32</sub>

</div>
