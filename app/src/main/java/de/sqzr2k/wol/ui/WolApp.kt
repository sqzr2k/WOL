package de.sqzr2k.wol.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import de.sqzr2k.wol.network.ScanResult
import de.sqzr2k.wol.ui.about.AboutScreen
import de.sqzr2k.wol.ui.deviceedit.DeviceEditScreen
import de.sqzr2k.wol.ui.deviceedit.ScanScreen
import de.sqzr2k.wol.ui.devices.DevicesScreen
import de.sqzr2k.wol.ui.groups.GroupsScreen
import de.sqzr2k.wol.ui.importexport.ImportExportScreen
import de.sqzr2k.wol.ui.settings.SettingsScreen
import de.sqzr2k.wol.ui.theme.WolTheme
import kotlinx.coroutines.launch

private sealed interface AppScreen {
    data class Devices(val groupId: Long? = null) : AppScreen
    data class EditDevice(val deviceId: Long? = null, val scanResult: ScanResult? = null) : AppScreen
    data object Scan : AppScreen
    data object Groups : AppScreen
    data object ImportExport : AppScreen
    data object Settings : AppScreen
    data object About : AppScreen
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WolApp(viewModel: AppViewModel) {
    val devices by viewModel.devices.collectAsState()
    val groups by viewModel.groups.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val statuses by viewModel.statuses.collectAsState()
    var screen: AppScreen by remember { mutableStateOf(AppScreen.Devices()) }
    var editBeforeScan: AppScreen.EditDevice? by remember { mutableStateOf(null) }
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> viewModel.setForeground(true)
                Lifecycle.Event.ON_PAUSE -> viewModel.setForeground(false)
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    LaunchedEffect(Unit) { viewModel.messages.collect { snackbarHostState.showSnackbar(it) } }

    fun navigate(target: AppScreen) {
        screen = target
        scope.launch { drawerState.close() }
    }

    val isRoot = screen is AppScreen.Devices
    BackHandler(!isRoot) { screen = AppScreen.Devices() }

    WolTheme(settings) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            gesturesEnabled = isRoot,
            drawerContent = {
                ModalDrawerSheet {
                    Text("WOL", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(24.dp))
                    NavigationDrawerItem(
                        label = { Text("Alle Geräte") },
                        selected = screen is AppScreen.Devices && (screen as AppScreen.Devices).groupId == null,
                        onClick = { navigate(AppScreen.Devices()) },
                    )
                    Text("Gruppen", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(horizontal = 28.dp, vertical = 8.dp))
                    groups.forEach { group ->
                        NavigationDrawerItem(
                            label = { Text(group.name) },
                            selected = (screen as? AppScreen.Devices)?.groupId == group.id,
                            onClick = { navigate(AppScreen.Devices(group.id)) },
                        )
                    }
                    HorizontalDivider(Modifier.padding(vertical = 8.dp))
                    NavigationDrawerItem({ Text("Gruppen verwalten") }, screen == AppScreen.Groups, { navigate(AppScreen.Groups) })
                    NavigationDrawerItem({ Text("Import / Export") }, screen == AppScreen.ImportExport, { navigate(AppScreen.ImportExport) })
                    NavigationDrawerItem({ Text("Einstellungen") }, screen == AppScreen.Settings, { navigate(AppScreen.Settings) })
                    NavigationDrawerItem({ Text("Über WOL") }, screen == AppScreen.About, { navigate(AppScreen.About) })
                }
            },
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text(titleFor(screen, groups.associate { it.id to it.name })) },
                        navigationIcon = {
                            IconButton(onClick = {
                                if (isRoot) scope.launch { drawerState.open() } else screen = AppScreen.Devices()
                            }) {
                                Icon(if (isRoot) Icons.Default.Menu else Icons.AutoMirrored.Filled.ArrowBack, null)
                            }
                        },
                    )
                },
                snackbarHost = { SnackbarHost(snackbarHostState) },
            ) { padding ->
                Box(Modifier.fillMaxSize().padding(padding)) {
                    when (val current = screen) {
                        is AppScreen.Devices -> DevicesScreen(
                            devices = devices.filter { current.groupId == null || it.groupId == current.groupId },
                            groups = groups,
                            statuses = statuses,
                            settings = settings,
                            selectedGroupId = current.groupId,
                            onWake = viewModel::wake,
                            onWakeGroup = viewModel::wakeGroup,
                            onAdd = { screen = AppScreen.EditDevice() },
                            onEdit = { screen = AppScreen.EditDevice(it.id) },
                        )
                        is AppScreen.EditDevice -> DeviceEditScreen(
                            existing = current.deviceId?.let { id -> devices.firstOrNull { it.id == id } },
                            groups = groups,
                            scanResult = current.scanResult,
                            suggestedBroadcast = viewModel.suggestedBroadcast(),
                            onSave = { viewModel.saveDevice(it) { screen = AppScreen.Devices() } },
                            onDelete = { viewModel.deleteDevice(it) { screen = AppScreen.Devices() } },
                            onScan = { editBeforeScan = current; screen = AppScreen.Scan },
                        )
                        AppScreen.Scan -> ScanScreen(
                            results = viewModel.scanResults.collectAsState().value,
                            progress = viewModel.scanProgress.collectAsState().value,
                            onStart = viewModel::startScan,
                            onCancel = viewModel::cancelScan,
                            onSelect = { result ->
                                val previous = editBeforeScan ?: AppScreen.EditDevice()
                                screen = previous.copy(scanResult = result)
                            },
                        )
                        AppScreen.Groups -> GroupsScreen(groups, viewModel::addGroup, viewModel::renameGroup, viewModel::deleteGroup)
                        AppScreen.ImportExport -> ImportExportScreen(viewModel)
                        AppScreen.Settings -> SettingsScreen(settings, viewModel::updateSettings)
                        AppScreen.About -> AboutScreen()
                    }
                }
            }
        }
    }
}

private fun titleFor(screen: AppScreen, groups: Map<Long, String>): String = when (screen) {
    is AppScreen.Devices -> screen.groupId?.let(groups::get) ?: "Geräte"
    is AppScreen.EditDevice -> if (screen.deviceId == null) "Gerät hinzufügen" else "Gerät bearbeiten"
    AppScreen.Scan -> "Gerät finden"
    AppScreen.Groups -> "Gruppen"
    AppScreen.ImportExport -> "Import / Export"
    AppScreen.Settings -> "Einstellungen"
    AppScreen.About -> "Über WOL"
}
