# WOL

Minimalistic open-source Wake-on-LAN app for Android.

WOL manages devices and groups locally and sends configurable IPv4 Wake-on-LAN packets. It supports direct LAN use as well as routed VPN and relay scenarios.

## Languages

- English
- German

WOL follows the Android system or per-app language. English is the fallback. On Android 13 and newer, the supported app languages are available in the system settings; the app does not provide a separate language selector.

## Features

- Wake-on-LAN with configurable target address and packet count
- Configurable UDP port and optional SecureOn password
- Local device and group management with group wake-up
- Online status checks using reachability and TCP ports
- Local IPv4 LAN scan
- Transactional CSV import and UTF-8 CSV export
- Route-aware VPN and WOL relay support
- Material 3 interface
- Light, dark, and system themes
- Compact display mode
- Fully local app configuration with Room and DataStore

## WOL relays

The WOL target is the address that actually receives the UDP magic packet. It may be a local broadcast address, a device IP address, a hostname, or a WOL relay. The device IP is separate and is used only to check the status of the device being woken.

Example configuration:

```text
WOL target:  192.168.10.20
UDP port:    40000
MAC address: 02:00:00:00:00:01
Device IP:   192.168.10.50

WOL app -> relay -> local broadcast -> target device
Status:  WOL app -> target device IP
```

For a resolved target address, WOL examines the routes exposed by the available Android networks. A uniquely most-specific non-default route can be selected and bound with `Network.bindSocket()`. VPN and Wi-Fi do not have a hard-coded priority. If there is no unambiguous specific route, Android's normal system routing remains in control.

## Privacy

- No accounts, cloud service, analytics, telemetry, advertising, or tracking SDKs
- No external APIs are used for app functionality
- Device data and settings remain on the Android device
- Network access is limited to user-configured WOL, status checks, LAN scanning, and route discovery
- CSV import and export happen only after an explicit user action

See [PRIVACY.md](PRIVACY.md) for details.

## Build

Requirements:

- JDK 17
- Android SDK Platform 36 and Build Tools 36.0.0
- The included Gradle Wrapper

From a Windows terminal:

```powershell
git clone <repository-url>
cd WOL
.\gradlew.bat assembleDebug
```

Run the unit tests and lint checks with:

```powershell
.\gradlew.bat testDebugUnitTest
.\gradlew.bat lintDebug
```

The debug APK is written to `app\build\outputs\apk\debug\app-debug.apk`.

## AI-assisted development

This project was developed with substantial AI-assisted and vibe-coded development. Changes are validated through builds, linting, automated tests, and practical device testing. This does not replace independent review or guarantee that the software is free of defects.

## Contributing

Issues and focused pull requests are welcome. See [CONTRIBUTING.md](CONTRIBUTING.md).

## Support

[![ko-fi](https://ko-fi.com/img/githubbutton_sm.svg)](https://ko-fi.com/X0O522RFAM)

## License

WOL is licensed under the Mozilla Public License 2.0 (`MPL-2.0`). See [LICENSE](LICENSE).
