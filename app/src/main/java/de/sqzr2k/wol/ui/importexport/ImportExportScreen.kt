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
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.sqzr2k.wol.R
import de.sqzr2k.wol.data.CsvSnapshot
import de.sqzr2k.wol.ui.AppViewModel
import java.io.IOException
import java.nio.charset.StandardCharsets
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun ImportExportScreen(viewModel: AppViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var pendingImport by remember { mutableStateOf<CsvSnapshot?>(null) }
    var error by remember { mutableStateOf<Int?>(null) }
    var exportText by remember { mutableStateOf<String?>(null) }

    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        scope.launch {
            val text = runCatching { readUtf8(context, uri) }.getOrElse { error = R.string.file_read_failed; return@launch }
            viewModel.parseCsv(text).onSuccess { pendingImport = it }.onFailure { error = R.string.csv_invalid }
        }
    }
    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
        val text = exportText
        if (uri != null && text != null) scope.launch {
            runCatching { writeUtf8(context, uri, text) }.onFailure { error = R.string.file_write_failed }
            exportText = null
        }
    }

    Column(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(stringResource(R.string.csv_import_export), style = MaterialTheme.typography.titleLarge)
        Text(stringResource(R.string.csv_description))
        Button({ importLauncher.launch(arrayOf("text/csv", "text/comma-separated-values", "text/plain")) }, Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.import_csv))
        }
        OutlinedButton(
            onClick = { viewModel.exportCsv { csv -> exportText = csv; exportLauncher.launch("wol-export.csv") } },
            modifier = Modifier.fillMaxWidth(),
        ) { Text(stringResource(R.string.export_csv)) }
        Text(stringResource(R.string.no_storage_permission), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }

    pendingImport?.let { snapshot ->
        val groupCount = pluralStringResource(R.plurals.validated_group_count, snapshot.groups.size, snapshot.groups.size)
        AlertDialog(
            onDismissRequest = { pendingImport = null }, title = { Text(stringResource(R.string.replace_data_title)) },
            text = { Text(pluralStringResource(R.plurals.replace_data_message, snapshot.devices.size, snapshot.devices.size, groupCount)) },
            confirmButton = { TextButton({ viewModel.importCsv(snapshot); pendingImport = null }) { Text(stringResource(R.string.import_action)) } },
            dismissButton = { TextButton({ pendingImport = null }) { Text(stringResource(R.string.cancel)) } },
        )
    }
    error?.let { messageResource ->
        AlertDialog(
            onDismissRequest = { error = null }, title = { Text(stringResource(R.string.file_processing_title)) },
            text = { Text(stringResource(messageResource)) }, confirmButton = { TextButton({ error = null }) { Text(stringResource(R.string.ok)) } },
        )
    }
}

private suspend fun readUtf8(context: Context, uri: Uri): String = withContext(Dispatchers.IO) {
    context.contentResolver.openInputStream(uri)?.bufferedReader(StandardCharsets.UTF_8)?.use { it.readText() }
        ?: throw IOException()
}

private suspend fun writeUtf8(context: Context, uri: Uri, value: String) = withContext(Dispatchers.IO) {
    context.contentResolver.openOutputStream(uri, "wt")?.bufferedWriter(StandardCharsets.UTF_8)?.use { it.write(value) }
        ?: throw IOException()
}
