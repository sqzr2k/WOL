# Engineering Learnings

- Das WOL-Magic-Packet lässt sich zuverlässig ohne externe Bibliothek erzeugen und per UDP an Broadcast-, Unicast- oder Relay-Ziele senden.
- Für lokale und VPN-basierte Ziele ist nicht die Transportart, sondern die spezifischste passende Android-Network-Route entscheidend; bei uneindeutiger Auswahl bleibt das Systemrouting zuständig.
- Androids `InetAddress.isReachable()` kann je nach Netzwerk/Hersteller unzuverlässig sein. Deshalb ergänzt WOL den Check um kurze TCP-Verbindungen.
- Moderne Android-Versionen geben ARP-/MAC-Informationen für fremde LAN-Geräte nicht zuverlässig frei. Scan-Ergebnisse bleiben daher auch ohne MAC-Adresse nutzbar.
- Ein auf `/24` begrenzter Scan mit Semaphore verhindert 254 gleichzeitig offene Prüfungen und bleibt abbrechbar.
- CSV wird vollständig geparst und validiert, bevor Room bestehende Daten innerhalb einer Transaktion ersetzt.
