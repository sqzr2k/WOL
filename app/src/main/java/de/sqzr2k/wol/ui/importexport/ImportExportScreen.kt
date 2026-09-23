package de.sqzr2k.wol.ui.importexport

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import de.sqzr2k.wol.data.CsvSnapshot
import de.sqzr2k.wol.ui.AppViewModel
import java.nio.charset.StandardCharsets
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun ImportExportScreen(viewModel: AppViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var pendingImport by remember { mutableStateOf<CsvSnapshot?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var exportText by remember { mutableStateOf<String?>(null) }

    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        scope.launch {
            val text = runCatching { readUtf8(context, uri) }.getOrElse { error = it.localizedMessage; return@launch }
            viewModel.parseCsv(text).onSuccess { pendingImport = it }.onFailure { error = it.localizedMessage }
        }
    }
    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
        val text = exportText
        if (uri != null && text != null) scope.launch {
            runCatching { writeUtf8(context, uri, text) }.onFailure { error = it.localizedMessage }
            exportText = null
        }
    }

    Column(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("CSV-Import/Export", style = MaterialTheme.typography.titleLarge)
        Text("Der Import einer CSV-Datei kann bestehende Daten ersetzen. Der Export erstellt eine UTF-8-CSV-Datei, die über den Android-Dateidialog gespeichert oder geteilt werden kann.")
        Button({ importLauncher.launch(arrayOf("text/csv", "text/comma-separated-values", "text/plain")) }, Modifier.fillMaxWidth()) {
            Text("CSV-Datei importieren")
        }
        OutlinedButton(
            onClick = { viewModel.exportCsv { csv -> exportText = csv; exportLauncher.launch("wol-export.csv") } },
            modifier = Modifier.fillMaxWidth(),
        ) { Text("CSV-Datei exportieren") }
        Text("Es werden keine pauschalen Speicherberechtigungen benötigt.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }

    pendingImport?.let { snapshot ->
        AlertDialog(
            onDismissRequest = { pendingImport = null }, title = { Text("Bestehende Daten ersetzen?") },
            text = { Text("${snapshot.devices.size} Geräte und ${snapshot.groups.size} Gruppen wurden validiert. Beim Fortfahren werden die aktuellen Daten transaktional ersetzt.") },
            confirmButton = { TextButton({ viewModel.importCsv(snapshot); pendingImport = null }) { Text("Importieren") } },
            dismissButton = { TextButton({ pendingImport = null }) { Text("Abbrechen") } },
        )
    }
    error?.let { message ->
        AlertDialog(
            onDismissRequest = { error = null }, title = { Text("Datei konnte nicht verarbeitet werden") },
            text = { Text(message ?: "Unbekannter Fehler") }, confirmButton = { TextButton({ error = null }) { Text("OK") } },
        )
    }
}

private suspend fun readUtf8(context: Context, uri: Uri): String = withContext(Dispatchers.IO) {
    context.contentResolver.openInputStream(uri)?.bufferedReader(StandardCharsets.UTF_8)?.use { it.readText() }
        ?: error("Datei konnte nicht geöffnet werden")
}

private suspend fun writeUtf8(context: Context, uri: Uri, value: String) = withContext(Dispatchers.IO) {
    context.contentResolver.openOutputStream(uri, "wt")?.bufferedWriter(StandardCharsets.UTF_8)?.use { it.write(value) }
        ?: error("Datei konnte nicht geschrieben werden")
}
