package de.sqzr2k.wol.network

internal data class NetworkRoute<T>(
    val network: T,
    val destination: ByteArray,
    val prefixLength: Int,
)

internal object NetworkRouteSelector {
    fun <T> select(target: ByteArray, routes: List<NetworkRoute<T>>): T? {
        val matching = routes.filter { route ->
            route.prefixLength > 0 && prefixMatches(target, route.destination, route.prefixLength)
        }
        val longestPrefix = matching.maxOfOrNull(NetworkRoute<T>::prefixLength) ?: return null
        return matching.asSequence()
            .filter { it.prefixLength == longestPrefix }
            .map(NetworkRoute<T>::network)
            .distinct()
            .singleOrNull()
    }

    private fun prefixMatches(target: ByteArray, destination: ByteArray, prefixLength: Int): Boolean {
        if (target.size != destination.size || prefixLength !in 0..target.size * Byte.SIZE_BITS) return false
        val completeBytes = prefixLength / Byte.SIZE_BITS
        for (index in 0 until completeBytes) {
            if (target[index] != destination[index]) return false
        }
        val remainingBits = prefixLength % Byte.SIZE_BITS
        if (remainingBits == 0) return true
        val mask = (0xFF shl (Byte.SIZE_BITS - remainingBits)) and 0xFF
        return (target[completeBytes].toInt() and mask) == (destination[completeBytes].toInt() and mask)
    }
}
