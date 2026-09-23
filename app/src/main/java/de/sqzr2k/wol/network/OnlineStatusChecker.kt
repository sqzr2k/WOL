package de.sqzr2k.wol.network

import de.sqzr2k.wol.data.local.DeviceEntity
import de.sqzr2k.wol.domain.model.OnlineState
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class OnlineStatusChecker {
    suspend fun check(device: DeviceEntity, globalPorts: List<Int>): OnlineState = withContext(Dispatchers.IO) {
        val host = device.deviceIp?.takeIf(String::isNotBlank)
            ?: device.hostname?.takeIf(String::isNotBlank)
            ?: return@withContext OnlineState.Unknown
        val started = System.nanoTime()
        val reachable = runCatching {
            device.pingCheckEnabled && InetAddress.getByName(host).isReachable(700)
        }.getOrDefault(false)
        if (reachable) return@withContext OnlineState.Online(elapsedMs(started))

        val ports = (listOfNotNull(device.manualPort) + globalPorts).distinct()
        for (port in ports) {
            val open = runCatching {
                Socket().use { it.connect(InetSocketAddress(host, port), 500) }
                true
            }.getOrDefault(false)
            if (open) return@withContext OnlineState.Online(elapsedMs(started))
        }
        if (!device.pingCheckEnabled && ports.isEmpty()) OnlineState.Unknown else OnlineState.Offline
    }

    private fun elapsedMs(started: Long) = ((System.nanoTime() - started) / 1_000_000).coerceAtLeast(1)
}
