package de.sqzr2k.wol.ui.devices

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.sqzr2k.wol.data.local.DeviceEntity
import de.sqzr2k.wol.data.local.GroupEntity
import de.sqzr2k.wol.data.settings.AppSettings
import de.sqzr2k.wol.domain.model.OnlineState

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DevicesScreen(
    devices: List<DeviceEntity>,
    groups: List<GroupEntity>,
    statuses: Map<Long, OnlineState>,
    settings: AppSettings,
    selectedGroupId: Long?,
    onWake: (DeviceEntity) -> Unit,
    onWakeGroup: (Long?) -> Unit,
    onAdd: () -> Unit,
    onEdit: (DeviceEntity) -> Unit,
) {
    val groupMap = groups.associateBy { it.id }
    val grouped = devices.groupBy { it.groupId }
    Box(Modifier.fillMaxSize()) {
        if (devices.isEmpty()) {
            Column(
                Modifier.align(Alignment.Center).padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("Noch keine Geräte", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Text("Füge mit + deinen ersten Rechner hinzu.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(Modifier.fillMaxSize(), contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 88.dp)) {
                val orderedKeys = if (selectedGroupId != null) listOf(selectedGroupId) else {
                    groups.map { it.id }.filter(grouped::containsKey) + listOf<Long?>(null).filter(grouped::containsKey)
                }
                orderedKeys.forEach { groupId ->
                    val groupDevices = grouped[groupId].orEmpty()
                    item(key = "header-$groupId") {
                        GroupHeader(
                            name = groupId?.let { groupMap[it]?.name } ?: "Ohne Gruppe",
                            canWake = groupDevices.isNotEmpty(),
                            onWake = { onWakeGroup(groupId) },
                        )
                    }
                    items(groupDevices, key = { it.id }) { device ->
                        DeviceRow(
                            device = device,
                            status = statuses[device.id] ?: OnlineState.Unknown,
                            showId = settings.showIds,
                            compact = settings.compactMode,
                            onWake = { onWake(device) },
                            onEdit = { onEdit(device) },
                        )
                    }
                }
            }
        }
        FloatingActionButton(onClick = onAdd, modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)) {
            Icon(Icons.Default.Add, contentDescription = "Gerät hinzufügen")
        }
    }
}

@Composable
private fun GroupHeader(name: String, canWake: Boolean, onWake: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceContainerLow).padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
        Button(onClick = onWake, enabled = canWake) { Text("Wecken") }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DeviceRow(
    device: DeviceEntity,
    status: OnlineState,
    showId: Boolean,
    compact: Boolean,
    onWake: () -> Unit,
    onEdit: () -> Unit,
) {
    val vertical = if (compact) 8.dp else 13.dp
    Surface(
        modifier = Modifier.fillMaxWidth().combinedClickable(onClick = onWake, onLongClick = onEdit),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Row(
            Modifier.padding(horizontal = 16.dp, vertical = vertical),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(Modifier.size(14.dp).background(Color(device.color), CircleShape))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (showId) Text("#${device.id}  ", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(device.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                }
                Text(device.macAddress, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Column(horizontalAlignment = Alignment.End) {
                Box(Modifier.size(10.dp).background(statusColor(status), CircleShape))
                if (status is OnlineState.Online) Text("${status.latencyMs} ms", style = MaterialTheme.typography.labelSmall)
                if (status is OnlineState.Checking) Text("…", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
private fun statusColor(status: OnlineState): Color = when (status) {
    is OnlineState.Online -> Color(0xFF2E7D32)
    OnlineState.Offline -> MaterialTheme.colorScheme.error
    OnlineState.Checking -> Color(0xFFF9A825)
    OnlineState.Unknown -> MaterialTheme.colorScheme.outline
}
