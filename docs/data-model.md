# Datenmodell

## Room, Schema 1

`DeviceEntity` speichert ID, Name, normalisierte MAC-Adresse, optionale Gruppe/Farbe/Host/IP/Broadcast/SecureOn/SSID, UDP-Port, Prüfschalter, manuellen TCP-Port und Zeitstempel.

`GroupEntity` speichert ID, eindeutigen Namen und Sortierreihenfolge. Der Fremdschlüssel eines Geräts verwendet `ON DELETE SET NULL`, sodass beim Löschen einer Gruppe keine Geräte verloren gehen.

Room-Schemas werden beim Build in `app/schemas` exportiert und bilden die Grundlage späterer Migrationstests.

## DataStore

DataStore Preferences speichert Paketanzahl, Designmodus, dynamische Farben, ID-Anzeige, Kompaktmodus, Statusintervall und globale TCP-Ports.

## Flüchtiger Zustand

`Unknown`, `Checking`, `Online(latencyMs)` und `Offline` werden nie in Room geschrieben.
