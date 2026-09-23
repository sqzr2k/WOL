# Architektur

WOL besteht bewusst aus einem einzigen Android-`app`-Modul.

```text
Compose UI + AppViewModel
          ↓
DeviceRepository / SettingsRepository
          ↓
Room / DataStore       WolSender / OnlineStatusChecker / LanScanner
```

`AppContainer` erzeugt die wenigen langlebigen Abhängigkeiten. Es gibt keine DI-Bibliothek. Die UI beobachtet `Flow`/`StateFlow`; Datenbank- und Netzwerkzugriffe laufen in Coroutines außerhalb des UI-Threads.

Die Navigation ist absichtlich klein und zustandsbasiert. Ein Material-3-Navigation-Drawer enthält die dynamischen Gruppen sowie Gruppenverwaltung, Import/Export, Einstellungen und Info.

Der Onlinestatus ist reiner Runtime-State im `AppViewModel`. Prüfungen laufen nur, solange die Activity aktiv ist. Es gibt keinen Hintergrunddienst und keine aggressiven Jobs.

## WOL-Routing und Status

`WolSender` löst die explizit konfigurierte WOL-Zieladresse auf und prüft für die verfügbaren Android-Networks `NetworkCapabilities`, `LinkProperties` und deren Routen. Die spezifischste eindeutig passende Nicht-Default-Route bestimmt das Network; eine pauschale Bevorzugung von VPN oder WLAN gibt es nicht. Bei einer eindeutigen Auswahl bindet `Network.bindSocket()` den UDP-Socket vor dem Versand. Fehlt eine passende Route oder ist die Auswahl uneindeutig, bleibt der Socket ungebunden und Android übernimmt das Systemrouting.

Die Geräte-IP ist nicht Teil dieses Sendepfads. `OnlineStatusChecker` verwendet sie für die Statusprüfung und fällt nur bei fehlender Geräte-IP auf den Hostnamen des Zielgeräts zurück. Dadurch können Relay-Ziel und überwachtes Endgerät unterschiedliche Adressen besitzen.
