# Architecture

WOL deliberately uses a single Android `app` module.

```text
Compose UI + AppViewModel
          ↓
DeviceRepository / SettingsRepository
          ↓
Room / DataStore       WolSender / OnlineStatusChecker / LanScanner
```

`AppContainer` creates the small set of long-lived dependencies. No dependency injection library is used. The UI observes `Flow`/`StateFlow`; database and network access runs in coroutines outside the UI thread.

Navigation is intentionally small and state-based. A Material 3 navigation drawer contains the dynamic groups as well as group management, import/export, settings, and about screens.

Online status is kept exclusively as runtime state in `AppViewModel`. Checks run only while the activity is active. There is no background service and no aggressive background work.

## WOL routing and status

`WolSender` resolves the explicitly configured WOL target address and inspects `NetworkCapabilities`, `LinkProperties`, and the routes of the available Android networks. The uniquely matching most-specific non-default route determines the network; neither VPN nor Wi-Fi receives blanket priority. When the selection is unambiguous, `Network.bindSocket()` binds the UDP socket before transmission. If no suitable route exists or the selection is ambiguous, the socket remains unbound and Android's system routing takes over.

The device IP is not part of this send path. `OnlineStatusChecker` uses it for online status checks and falls back to the target device hostname only when no device IP is configured. This allows the WOL relay target and the monitored end device to use different addresses.
