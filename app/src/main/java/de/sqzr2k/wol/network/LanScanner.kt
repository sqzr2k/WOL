package de.sqzr2k.wol.network

import java.net.Inet4Address
import java.net.InetAddress
import java.net.NetworkInterface
import java.util.Collections
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext

data class ScanResult(
    val ipAddress: String,
    val hostname: String?,
    val macAddress: String? = null,
    val latencyMs: Long,
)

class LanScanner {
    fun currentBroadcastAddress(): String? = localInterface()?.interfaceAddresses
        ?.firstOrNull { it.address is Inet4Address }
        ?.broadcast
        ?.hostAddress

    suspend fun scan(onProgress: suspend (Int) -> Unit): List<ScanResult> = withContext(Dispatchers.IO) {
        val address = localInterface()?.inetAddresses?.let(Collections::list)
            ?.filterIsInstance<Inet4Address>()
            ?.firstOrNull { !it.isLoopbackAddress }
            ?: return@withContext emptyList()
        val octets = address.address.map(Byte::toInt).map { it and 0xFF }
        val prefix = "${octets[0]}.${octets[1]}.${octets[2]}"
        val semaphore = Semaphore(24)
        var completed = 0
        coroutineScope {
            (1..254).map { host ->
                async {
                    semaphore.withPermit {
                        val ip = "$prefix.$host"
                        val started = System.nanoTime()
                        val target = InetAddress.getByName(ip)
                        val reachable = runCatching { target.isReachable(350) }.getOrDefault(false)
                        synchronized(this@LanScanner) { completed += 1 }
                        onProgress(completed)
                        if (reachable) {
                            val hostname = target.canonicalHostName.takeUnless { it == ip }
                            ScanResult(ip, hostname, null, ((System.nanoTime() - started) / 1_000_000).coerceAtLeast(1))
                        } else null
                    }
                }
            }.awaitAll().filterNotNull().sortedBy { result -> result.ipAddress.substringAfterLast('.').toInt() }
        }
    }

    private fun localInterface(): NetworkInterface? = Collections.list(NetworkInterface.getNetworkInterfaces())
        .firstOrNull { network ->
            runCatching {
                network.isUp && !network.isLoopback && Collections.list(network.inetAddresses)
                    .filterIsInstance<Inet4Address>().any { it.isSiteLocalAddress }
            }.getOrDefault(false)
        }
}
