package de.sqzr2k.wol.data.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

enum class ThemeMode { SYSTEM, LIGHT, DARK }

data class AppSettings(
    val packetCount: Int = 3,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val showIds: Boolean = false,
    val compactMode: Boolean = false,
    val refreshSeconds: Int = 30,
    val onlinePorts: String = "22,80,443,3389,445",
    val dynamicColor: Boolean = true,
)

class SettingsRepository(private val context: Context) {
    private object Keys {
        val packetCount = intPreferencesKey("packet_count")
        val themeMode = stringPreferencesKey("theme_mode")
        val showIds = booleanPreferencesKey("show_ids")
        val compactMode = booleanPreferencesKey("compact_mode")
        val refreshSeconds = intPreferencesKey("refresh_seconds")
        val onlinePorts = stringPreferencesKey("online_ports")
        val dynamicColor = booleanPreferencesKey("dynamic_color")
    }

    val settings: Flow<AppSettings> = context.dataStore.data.map { prefs ->
        AppSettings(
            packetCount = prefs[Keys.packetCount] ?: 3,
            themeMode = runCatching { ThemeMode.valueOf(prefs[Keys.themeMode] ?: "SYSTEM") }.getOrDefault(ThemeMode.SYSTEM),
            showIds = prefs[Keys.showIds] ?: false,
            compactMode = prefs[Keys.compactMode] ?: false,
            refreshSeconds = prefs[Keys.refreshSeconds] ?: 30,
            onlinePorts = prefs[Keys.onlinePorts] ?: "22,80,443,3389,445",
            dynamicColor = prefs[Keys.dynamicColor] ?: true,
        )
    }

    suspend fun update(value: AppSettings) {
        context.dataStore.edit { prefs ->
            prefs[Keys.packetCount] = value.packetCount.coerceIn(1, 10)
            prefs[Keys.themeMode] = value.themeMode.name
            prefs[Keys.showIds] = value.showIds
            prefs[Keys.compactMode] = value.compactMode
            prefs[Keys.refreshSeconds] = value.refreshSeconds.coerceIn(5, 3600)
            prefs[Keys.onlinePorts] = value.onlinePorts
            prefs[Keys.dynamicColor] = value.dynamicColor
        }
    }
}
