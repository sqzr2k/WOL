package de.sqzr2k.wol.ui.deviceedit

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import de.sqzr2k.wol.R
import de.sqzr2k.wol.data.local.DeviceEntity
import de.sqzr2k.wol.data.local.GroupEntity
import de.sqzr2k.wol.domain.wol.MacAddress
import de.sqzr2k.wol.network.ScanResult

private val palette = listOf(0xFF315DA8, 0xFF2E7D32, 0xFFC62828, 0xFF6A1B9A, 0xFFEF6C00, 0xFF00695C)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DeviceEditScreen(
    existing: DeviceEntity?,
    groups: List<GroupEntity>,
    scanResult: ScanResult?,
    suggestedBroadcast: String,
    onSave: (DeviceEntity) -> Unit,
    onDelete: (DeviceEntity) -> Unit,
    onScan: () -> Unit,
) {
    var name by remember(existing?.id, scanResult) { mutableStateOf(existing?.name ?: scanResult?.hostname.orEmpty()) }
    var mac by remember(existing?.id, scanResult) { mutableStateOf(existing?.macAddress ?: scanResult?.macAddress.orEmpty()) }
    var groupId by remember(existing?.id) { mutableStateOf(existing?.groupId) }
    var color by remember(existing?.id) { mutableLongStateOf(existing?.color ?: palette.first()) }
    var hostname by remember(existing?.id, scanResult) { mutableStateOf(existing?.hostname ?: scanResult?.hostname.orEmpty()) }
    var deviceIp by remember(existing?.id, scanResult) { mutableStateOf(existing?.deviceIp ?: scanResult?.ipAddress.orEmpty()) }
    var broadcast by remember(existing?.id) { mutableStateOf(existing?.broadcastAddress ?: suggestedBroadcast) }
    var port by remember(existing?.id) { mutableStateOf((existing?.port ?: 9).toString()) }
    var secureOn by remember(existing?.id) { mutableStateOf(existing?.secureOnPassword.orEmpty()) }
    var wifiSsid by remember(existing?.id) { mutableStateOf(existing?.wifiSsid.orEmpty()) }
    var pingCheck by remember(existing?.id) { mutableStateOf(existing?.pingCheckEnabled ?: true) }
    var manualPort by remember(existing?.id) { mutableStateOf(existing?.manualPort?.toString().orEmpty()) }
    var groupExpanded by remember { mutableStateOf(false) }
    var deleteConfirm by remember { mutableStateOf(false) }
    var attempted by remember { mutableStateOf(false) }

    val normalizedMac = MacAddress.normalize(mac)
    val validSecureOn = secureOn.isBlank() || MacAddress.normalizeSecureOn(secureOn) != null
    val validPort = port.toIntOrNull()?.let { it in 1..65535 } == true
    val validManualPort = manualPort.isBlank() || manualPort.toIntOrNull()?.let { it in 1..65535 } == true
    val valid = name.isNotBlank() && normalizedMac != null && validSecureOn && validPort && validManualPort

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                OutlinedButton(onClick = onScan) { Text(stringResource(R.string.search_network)) }
            }
        }
        item { OutlinedTextField(name, { name = it }, label = { Text(stringResource(R.string.device_name_required)) }, isError = attempted && name.isBlank(), singleLine = true, modifier = Modifier.fillMaxWidth()) }
        item {
            OutlinedTextField(
                mac, { mac = it }, label = { Text(stringResource(R.string.mac_address_required)) },
                supportingText = { if (attempted && normalizedMac == null) Text(stringResource(R.string.mac_address_example)) },
                isError = attempted && normalizedMac == null, singleLine = true, modifier = Modifier.fillMaxWidth(),
            )
        }
        item {
            ExposedDropdownMenuBox(expanded = groupExpanded, onExpandedChange = { groupExpanded = it }) {
                OutlinedTextField(
                    value = groups.firstOrNull { it.id == groupId }?.name ?: stringResource(R.string.no_group),
                    onValueChange = {}, readOnly = true, label = { Text(stringResource(R.string.group)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(groupExpanded) },
                    modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                )
                ExposedDropdownMenu(expanded = groupExpanded, onDismissRequest = { groupExpanded = false }) {
                    DropdownMenuItem({ Text(stringResource(R.string.no_group)) }, onClick = { groupId = null; groupExpanded = false })
                    groups.forEach { group ->
                        DropdownMenuItem({ Text(group.name) }, onClick = { groupId = group.id; groupExpanded = false })
                    }
                }
            }
        }
        item {
            Text(stringResource(R.string.color), style = MaterialTheme.typography.labelLarge)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.padding(top = 8.dp)) {
                palette.forEach { value ->
                    Box(
                        Modifier.size(if (color == value) 34.dp else 28.dp).background(Color(value), CircleShape).clickable { color = value },
                    )
                }
            }
        }
        item { HorizontalDivider(); Text(stringResource(R.string.network), style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(top = 8.dp)) }
        item { OutlinedTextField(hostname, { hostname = it }, label = { Text(stringResource(R.string.target_device_hostname)) }, singleLine = true, modifier = Modifier.fillMaxWidth()) }
        item { OutlinedTextField(deviceIp, { deviceIp = it }, label = { Text(stringResource(R.string.device_ip)) }, singleLine = true, modifier = Modifier.fillMaxWidth()) }
        item {
            OutlinedTextField(
                broadcast, { broadcast = it }, label = { Text(stringResource(R.string.wol_target_address)) },
                supportingText = { Text(stringResource(R.string.target_address_hint)) },
                singleLine = true, modifier = Modifier.fillMaxWidth(),
            )
        }
        item {
            OutlinedTextField(
                port, { port = it.filter(Char::isDigit) }, label = { Text(stringResource(R.string.udp_port)) }, isError = attempted && !validPort,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, modifier = Modifier.fillMaxWidth(),
            )
        }
        item {
            OutlinedTextField(
                secureOn, { secureOn = it }, label = { Text(stringResource(R.string.secureon_password)) },
                supportingText = { Text(stringResource(R.string.secureon_hint)) }, isError = attempted && !validSecureOn,
                singleLine = true, modifier = Modifier.fillMaxWidth(),
            )
        }
        item { OutlinedTextField(wifiSsid, { wifiSsid = it }, label = { Text(stringResource(R.string.wifi_ssid)) }, singleLine = true, modifier = Modifier.fillMaxWidth()) }
        item { HorizontalDivider(); Text(stringResource(R.string.online_status), style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(top = 8.dp)) }
        item { SettingSwitch(stringResource(R.string.check_reachability), pingCheck, { pingCheck = it }) }
        item {
            OutlinedTextField(
                manualPort, { manualPort = it.filter(Char::isDigit) }, label = { Text(stringResource(R.string.manual_tcp_port)) },
                supportingText = { Text(stringResource(R.string.manual_tcp_port_hint)) }, isError = attempted && !validManualPort,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, modifier = Modifier.fillMaxWidth(),
            )
        }
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.netbios_check))
                    Text(stringResource(R.string.not_available_in_version), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Checkbox(checked = false, onCheckedChange = null, enabled = false)
            }
        }
        item {
            Button(
                onClick = {
                    attempted = true
                    if (valid) onSave(
                        (existing ?: DeviceEntity(name = "", macAddress = normalizedMac!!)).copy(
                            name = name.trim(), macAddress = normalizedMac, groupId = groupId, color = color,
                            hostname = hostname.trim().ifBlank { null }, deviceIp = deviceIp.trim().ifBlank { null },
                            broadcastAddress = broadcast.trim().ifBlank { null }, port = port.toInt(),
                            secureOnPassword = secureOn.takeIf(String::isNotBlank)?.let(MacAddress::normalizeSecureOn),
                            wifiSsid = wifiSsid.trim().ifBlank { null }, pingCheckEnabled = pingCheck,
                            netbiosCheckEnabled = false, manualPort = manualPort.toIntOrNull(),
                        ),
                    )
                },
                modifier = Modifier.fillMaxWidth(),
            ) { Text(stringResource(R.string.save)) }
        }
        if (existing != null) {
            item { OutlinedButton({ deleteConfirm = true }, Modifier.fillMaxWidth()) { Text(stringResource(R.string.delete_device)) } }
        }
    }
    if (deleteConfirm && existing != null) {
        AlertDialog(
            onDismissRequest = { deleteConfirm = false }, title = { Text(stringResource(R.string.delete_device_title)) },
            text = { Text(stringResource(R.string.delete_device_message, existing.name)) },
            confirmButton = { TextButton({ deleteConfirm = false; onDelete(existing) }) { Text(stringResource(R.string.delete)) } },
            dismissButton = { TextButton({ deleteConfirm = false }) { Text(stringResource(R.string.cancel)) } },
        )
    }
}

@Composable
private fun SettingSwitch(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, modifier = Modifier.weight(1f))
        Switch(checked, onCheckedChange)
    }
}

@Composable
fun ScanScreen(
    results: List<ScanResult>,
    progress: Int?,
    onStart: () -> Unit,
    onCancel: () -> Unit,
    onSelect: (ScanResult) -> Unit,
) {
    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            if (progress == null) Button(onClick = onStart) { Text(stringResource(if (results.isEmpty()) R.string.start_scan else R.string.scan_again)) }
            else {
                OutlinedButton(onClick = onCancel) { Text(stringResource(R.string.cancel)) }
                CircularProgressIndicator(Modifier.size(24.dp))
                Text(stringResource(R.string.scan_progress, progress))
            }
        }
        if (results.isEmpty() && progress == null) {
            Text(stringResource(R.string.scan_description), Modifier.padding(16.dp))
        }
        LazyColumn(Modifier.fillMaxSize()) {
            items(results, key = { it.ipAddress }) { result ->
                Column(Modifier.fillMaxWidth().clickable { onSelect(result) }.padding(16.dp)) {
                    Text(result.hostname?.let { stringResource(R.string.scan_result_hostname, it, result.ipAddress) } ?: result.ipAddress, style = MaterialTheme.typography.titleSmall)
                    Text(result.macAddress ?: stringResource(R.string.mac_address_not_found), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(stringResource(R.string.latency_milliseconds, result.latencyMs), style = MaterialTheme.typography.labelSmall)
                }
                HorizontalDivider()
            }
        }
    }
}
