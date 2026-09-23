package de.sqzr2k.wol

import android.content.Context
import androidx.room.Room
import de.sqzr2k.wol.data.local.WolDatabase
import de.sqzr2k.wol.data.repository.DeviceRepository
import de.sqzr2k.wol.data.settings.SettingsRepository
import de.sqzr2k.wol.network.LanScanner
import de.sqzr2k.wol.network.OnlineStatusChecker
import de.sqzr2k.wol.network.WolSender

class AppContainer(context: Context) {
    private val database = Room.databaseBuilder(
        context.applicationContext,
        WolDatabase::class.java,
        "wol.db",
    ).build()

    val devices = DeviceRepository(database)
    val settings = SettingsRepository(context.applicationContext)
    val wolSender = WolSender(context.applicationContext)
    val statusChecker = OnlineStatusChecker()
    val lanScanner = LanScanner()
}
