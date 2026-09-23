package de.sqzr2k.wol.ui.groups

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.sqzr2k.wol.data.local.GroupEntity

@Composable
fun GroupsScreen(
    groups: List<GroupEntity>,
    onAdd: (String) -> Unit,
    onRename: (GroupEntity, String) -> Unit,
    onDelete: (GroupEntity) -> Unit,
) {
    var newName by remember { mutableStateOf("") }
    var editing by remember { mutableStateOf<GroupEntity?>(null) }
    var deleting by remember { mutableStateOf<GroupEntity?>(null) }
    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(newName, { newName = it }, label = { Text("Neue Gruppe") }, singleLine = true, modifier = Modifier.weight(1f))
            Button(onClick = { onAdd(newName); newName = "" }, enabled = newName.isNotBlank()) { Text("Hinzufügen") }
        }
        if (groups.isEmpty()) Text("Noch keine Gruppen angelegt.", Modifier.padding(16.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
        LazyColumn(contentPadding = PaddingValues(bottom = 16.dp)) {
            items(groups, key = { it.id }) { group ->
                Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(group.name, Modifier.weight(1f))
                    TextButton({ editing = group }) { Text("Umbenennen") }
                    TextButton({ deleting = group }) { Text("Löschen") }
                }
                HorizontalDivider()
            }
        }
    }
    editing?.let { group ->
        var value by remember(group.id) { mutableStateOf(group.name) }
        AlertDialog(
            onDismissRequest = { editing = null }, title = { Text("Gruppe umbenennen") },
            text = { OutlinedTextField(value, { value = it }, singleLine = true) },
            confirmButton = { TextButton({ onRename(group, value); editing = null }, enabled = value.isNotBlank()) { Text("Speichern") } },
            dismissButton = { TextButton({ editing = null }) { Text("Abbrechen") } },
        )
    }
    deleting?.let { group ->
        AlertDialog(
            onDismissRequest = { deleting = null }, title = { Text("Gruppe löschen?") },
            text = { Text("Geräte bleiben erhalten und werden ‚Keine Gruppe‘ zugeordnet.") },
            confirmButton = { TextButton({ onDelete(group); deleting = null }) { Text("Löschen") } },
            dismissButton = { OutlinedButton({ deleting = null }) { Text("Abbrechen") } },
        )
    }
}
