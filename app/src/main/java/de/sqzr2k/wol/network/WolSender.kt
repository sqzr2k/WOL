package de.sqzr2k.wol.network

import android.content.Context
import android.content.pm.ApplicationInfo
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.RouteInfo
import android.os.Build
import android.util.Log
import de.sqzr2k.wol.data.local.DeviceEntity
import de.sqzr2k.wol.domain.wol.WolPacket
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class WolSender(context: Context) {
    private val connectivityManager = context.getSystemService(ConnectivityManager::class.java)
    private val debugLogging = context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0

    suspend fun send(device: DeviceEntity, packetCount: Int) = withContext(Dispatchers.IO) {
        val bytes = WolPacket.build(device.macAddress, device.secureOnPassword)
        val address = InetAddress.getByName(device.broadcastAddress?.takeIf(String::isNotBlank) ?: "255.255.255.255")
        val selectedNetwork = selectNetwork(address)
        val (socket, networkBound) = createSocket(selectedNetwork)
        val count = packetCount.coerceIn(1, 10)
        socket.use {
            repeat(count) { index ->
                debugLog(
                    "target=${address.hostAddress}:${device.port} " +
                        "transport=${selectedNetwork?.transport ?: "SYSTEM"} " +
                        "networkBound=$networkBound bytes=${bytes.size} packet=${index + 1}/$count",
                )
                socket.send(DatagramPacket(bytes, bytes.size, address, device.port))
                if (index < count - 1) delay(35)
            }
        }
    }

    private fun selectNetwork(target: InetAddress): SelectedNetwork? {
        val routes = connectivityManager.allNetworks.flatMap { network ->
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return@flatMap emptyList()
            val linkProperties = connectivityManager.getLinkProperties(network) ?: return@flatMap emptyList()
            val selectedNetwork = SelectedNetwork(network, transportName(capabilities))
            linkProperties.routes
                .filter(::isUsableRoute)
                .map { route ->
                    NetworkRoute(
                        network = selectedNetwork,
                        destination = route.destination.address.address,
                        prefixLength = route.destination.prefixLength,
                    )
                }
        }
        return NetworkRouteSelector.select(target.address, routes)
    }

    private fun isUsableRoute(route: RouteInfo): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU || route.type == RouteInfo.RTN_UNICAST

    private fun createSocket(selectedNetwork: SelectedNetwork?): Pair<DatagramSocket, Boolean> {
        val socket = DatagramSocket().apply { broadcast = true }
        if (selectedNetwork == null) return socket to false
        return runCatching {
            selectedNetwork.network.bindSocket(socket)
            socket to true
        }.getOrElse { error ->
            socket.close()
            debugLog("network binding failed (${error.javaClass.simpleName}); using system routing")
            DatagramSocket().apply { broadcast = true } to false
        }
    }

    private fun transportName(capabilities: NetworkCapabilities?): String {
        if (capabilities == null) return "UNKNOWN"
        return buildList {
            if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) add("WIFI")
            if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) add("ETHERNET")
            if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN)) add("VPN")
            if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) add("CELLULAR")
        }.joinToString("+").ifEmpty { "OTHER" }
    }

    private fun debugLog(message: String) {
        if (debugLogging) Log.d(LOG_TAG, message)
    }

    private data class SelectedNetwork(val network: Network, val transport: String)

    private companion object {
        const val LOG_TAG = "WolSender"
    }
}
