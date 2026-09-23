package de.sqzr2k.wol.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import de.sqzr2k.wol.data.settings.AppSettings
import de.sqzr2k.wol.data.settings.ThemeMode

@Composable
fun SettingsScreen(settings: AppSettings, onSave: (AppSettings) -> Unit) {
    var edited by remember(settings) { mutableStateOf(settings) }
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { SectionTitle("Allgemein") }
        item {
            OutlinedTextField(
                edited.packetCount.toString(),
                { value -> value.toIntOrNull()?.let { edited = edited.copy(packetCount = it.coerceIn(1, 10)) } },
                label = { Text("WOL Packet Count") }, supportingText = { Text("1 bis 10; Standard: 3") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, modifier = Modifier.fillMaxWidth(),
            )
        }
        item { SectionTitle("Geräteliste") }
        item { Toggle("ID-Nummern anzeigen", edited.showIds) { edited = edited.copy(showIds = it) } }
        item { Toggle("Kompaktmodus", edited.compactMode) { edited = edited.copy(compactMode = it) } }
        item {
            OutlinedTextField(
                edited.refreshSeconds.toString(),
                { value -> value.toIntOrNull()?.let { edited = edited.copy(refreshSeconds = it.coerceIn(5, 3600)) } },
                label = { Text("Onlinestatus-Erneuerung (Sekunden)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, modifier = Modifier.fillMaxWidth(),
            )
        }
        item { SectionTitle("Onlinestatus") }
        item {
            OutlinedTextField(
                edited.onlinePorts, { edited = edited.copy(onlinePorts = it) },
                label = { Text("Globale TCP-Ports") }, supportingText = { Text("Kommagetrennt, z. B. 22,80,443,3389,445") },
                singleLine = true, modifier = Modifier.fillMaxWidth(),
            )
        }
        item { SectionTitle("Darstellung") }
        item {
            Column {
                ThemeMode.entries.forEach { mode ->
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(edited.themeMode == mode, { edited = edited.copy(themeMode = mode) })
                        Text(when (mode) { ThemeMode.SYSTEM -> "System"; ThemeMode.LIGHT -> "Hell"; ThemeMode.DARK -> "Dunkel" })
                    }
                }
            }
        }
        item { Toggle("Dynamische Farben", edited.dynamicColor) { edited = edited.copy(dynamicColor = it) } }
        item { Button({ onSave(edited) }, Modifier.fillMaxWidth()) { Text("Einstellungen speichern") } }
    }
}

@Composable
private fun SectionTitle(value: String) {
    HorizontalDivider(Modifier.padding(top = 4.dp))
    Text(value, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 8.dp))
}

@Composable
private fun Toggle(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, Modifier.weight(1f))
        Switch(checked, onChange)
    }
}
