package de.sqzr2k.wol.ui.about

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AboutScreen() {
    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("WOL", style = MaterialTheme.typography.headlineMedium)
        Text("Version 0.1.0-dev")
        Text("Eine kleine, unabhängige FOSS-App zum Senden von Wake-on-LAN-Paketen.")
        Text("Keine Accounts · Keine Cloud · Keine Werbung · Keine Telemetrie", color = MaterialTheme.colorScheme.primary)
        Text("Alle Geräte, Gruppen und Einstellungen bleiben lokal auf diesem Gerät. Netzwerkverkehr entsteht nur durch ausdrücklich ausgelöste Wake-Pakete, lokale Statusprüfungen und LAN-Scans.")
        Text("Lizenz: Mozilla Public License 2.0")
    }
}
