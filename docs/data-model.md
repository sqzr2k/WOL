# Data model

## Room, schema 1

`DeviceEntity` stores the ID, name, normalized MAC address, optional group, color, hostname, device IP, broadcast address, SecureOn password, SSID, UDP port, check flags, manual TCP port, and timestamps.

`GroupEntity` stores the ID, unique name, and sort order. A device's foreign key uses `ON DELETE SET NULL`, so deleting a group does not delete any devices.

Room schemas are exported to `app/schemas` during the build and provide the basis for future migration tests.

## DataStore

DataStore Preferences stores the packet count, theme mode, dynamic color setting, ID display setting, compact mode, online status interval, and global TCP ports.

## Transient state

`Unknown`, `Checking`, `Online(latencyMs)`, and `Offline` are never written to Room.
