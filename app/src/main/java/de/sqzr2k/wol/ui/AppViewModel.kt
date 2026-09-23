package de.sqzr2k.wol.ui

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import de.sqzr2k.wol.AppContainer
import de.sqzr2k.wol.R
import de.sqzr2k.wol.data.CsvCodec
import de.sqzr2k.wol.data.CsvSnapshot
import de.sqzr2k.wol.data.local.DeviceEntity
import de.sqzr2k.wol.data.local.GroupEntity
import de.sqzr2k.wol.data.settings.AppSettings
import de.sqzr2k.wol.domain.model.OnlineState
import de.sqzr2k.wol.network.ScanResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class UiMessage(
    @param:StringRes val resourceId: Int,
    val args: List<Any> = emptyList(),
)

class AppViewModel(private val container: AppContainer) : ViewModel() {
    val devices = container.devices.devices.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val groups = container.devices.groups.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val settings = container.settings.settings.stateIn(viewModelScope, SharingStarted.Eagerly, AppSettings())

    private val _statuses = MutableStateFlow<Map<Long, OnlineState>>(emptyMap())
    val statuses: StateFlow<Map<Long, OnlineState>> = _statuses.asStateFlow()
    private val _messages = MutableSharedFlow<UiMessage>(extraBufferCapacity = 8)
    val messages = _messages.asSharedFlow()
    private val _foreground = MutableStateFlow(true)
    private var monitorJob: Job? = null

    private val _scanResults = MutableStateFlow<List<ScanResult>>(emptyList())
    val scanResults = _scanResults.asStateFlow()
    private val _scanProgress = MutableStateFlow<Int?>(null)
    val scanProgress = _scanProgress.asStateFlow()
    private var scanJob: Job? = null

    init { startMonitoring() }

    fun setForeground(value: Boolean) { _foreground.value = value }

    fun saveDevice(device: DeviceEntity, onSaved: () -> Unit = {}) = viewModelScope.launch {
        runCatching { container.devices.saveDevice(device) }
            .onSuccess { onSaved() }
            .onFailure { _messages.emit(UiMessage(R.string.message_device_save_failed)) }
    }

    fun deleteDevice(device: DeviceEntity, onDeleted: () -> Unit = {}) = viewModelScope.launch {
        runCatching { container.devices.deleteDevice(device) }
            .onSuccess { onDeleted() }
            .onFailure { _messages.emit(UiMessage(R.string.message_device_delete_failed)) }
    }

    fun addGroup(name: String) = viewModelScope.launch {
        if (name.isBlank()) return@launch
        runCatching { container.devices.addGroup(name) }
            .onFailure { _messages.emit(UiMessage(R.string.message_group_add_failed)) }
    }

    fun renameGroup(group: GroupEntity, name: String) = viewModelScope.launch {
        runCatching { container.devices.updateGroup(group.copy(name = name.trim())) }
            .onFailure { _messages.emit(UiMessage(R.string.message_group_rename_failed)) }
    }

    fun deleteGroup(group: GroupEntity) = viewModelScope.launch {
        runCatching { container.devices.deleteGroup(group) }
            .onFailure { _messages.emit(UiMessage(R.string.message_group_delete_failed)) }
    }

    fun wake(device: DeviceEntity) = viewModelScope.launch {
        runCatching { container.wolSender.send(device, settings.value.packetCount) }
            .onSuccess { _messages.emit(UiMessage(R.string.message_wake_sent, listOf(device.name))) }
            .onFailure { _messages.emit(UiMessage(R.string.message_wake_failed, listOf(device.name))) }
    }

    fun wakeGroup(groupId: Long?) {
        devices.value.filter { it.groupId == groupId }.forEach(::wake)
    }

    fun updateSettings(value: AppSettings) = viewModelScope.launch { container.settings.update(value) }

    fun exportCsv(onReady: (String) -> Unit) = viewModelScope.launch {
        val (deviceList, groupList) = container.devices.snapshot()
        onReady(CsvCodec.encode(deviceList, groupList))
    }

    fun parseCsv(value: String): Result<CsvSnapshot> = runCatching { CsvCodec.decode(value) }

    fun importCsv(snapshot: CsvSnapshot, onDone: () -> Unit = {}) = viewModelScope.launch {
        runCatching { container.devices.replaceAll(snapshot.devices, snapshot.groups) }
            .onSuccess { _messages.emit(UiMessage(R.string.message_csv_imported)); onDone() }
            .onFailure { _messages.emit(UiMessage(R.string.message_csv_import_failed)) }
    }

    fun suggestedBroadcast() = container.lanScanner.currentBroadcastAddress() ?: "255.255.255.255"

    fun startScan() {
        scanJob?.cancel()
        _scanResults.value = emptyList()
        _scanProgress.value = 0
        scanJob = viewModelScope.launch {
            try {
                _scanResults.value = container.lanScanner.scan { _scanProgress.value = it }
            } catch (_: CancellationException) {
                _messages.emit(UiMessage(R.string.message_scan_cancelled))
            } catch (_: Exception) {
                _messages.emit(UiMessage(R.string.message_scan_failed))
            } finally {
                _scanProgress.value = null
            }
        }
    }

    fun cancelScan() { scanJob?.cancel() }

    private fun startMonitoring() {
        monitorJob?.cancel()
        monitorJob = viewModelScope.launch {
            while (isActive) {
                if (_foreground.value) refreshStatuses()
                delay(settings.value.refreshSeconds.coerceIn(5, 3600) * 1_000L)
            }
        }
    }

    private suspend fun refreshStatuses() {
        val ports = settings.value.onlinePorts.split(',').mapNotNull { it.trim().toIntOrNull() }
            .filter { it in 1..65535 }.distinct()
        devices.value.forEach { device ->
            if (!device.pingCheckEnabled && device.manualPort == null && ports.isEmpty()) {
                _statuses.value += device.id to OnlineState.Unknown
            } else {
                _statuses.value += device.id to OnlineState.Checking
                _statuses.value += device.id to container.statusChecker.check(device, ports)
            }
        }
    }

    companion object {
        fun factory(container: AppContainer) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = AppViewModel(container) as T
        }
    }
}
