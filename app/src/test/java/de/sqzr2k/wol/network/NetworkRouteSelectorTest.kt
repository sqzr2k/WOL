package de.sqzr2k.wol.network

import java.net.InetAddress
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class NetworkRouteSelectorTest {
    @Test
    fun `vpn route is selected for its remote subnet`() {
        val selected = select(
            target = "192.0.2.255",
            route("vpn", "192.0.2.0", 24),
            route("wifi", "198.51.100.0", 24),
        )

        assertEquals("vpn", selected)
    }

    @Test
    fun `wifi route is selected for its local subnet`() {
        val selected = select(
            target = "198.51.100.255",
            route("wifi", "198.51.100.0", 24),
            route("vpn", "192.0.2.0", 24),
        )

        assertEquals("wifi", selected)
    }

    @Test
    fun `most specific matching route wins`() {
        val selected = select(
            target = "192.0.2.255",
            route("vpn", "192.0.0.0", 16),
            route("relay", "192.0.2.0", 24),
        )

        assertEquals("relay", selected)
    }

    @Test
    fun `default or unrelated routes keep system routing`() {
        val selected = select(
            target = "192.0.2.255",
            route("default", "0.0.0.0", 0),
            route("wifi", "198.51.100.0", 24),
        )

        assertNull(selected)
    }

    @Test
    fun `equally specific routes keep system routing`() {
        val selected = select(
            target = "192.0.2.255",
            route("vpn", "192.0.2.0", 24),
            route("wifi", "192.0.2.0", 24),
        )

        assertNull(selected)
    }

    private fun select(target: String, vararg routes: NetworkRoute<String>): String? =
        NetworkRouteSelector.select(address(target), routes.toList())

    private fun route(network: String, destination: String, prefixLength: Int) =
        NetworkRoute(network, address(destination), prefixLength)

    private fun address(value: String): ByteArray = InetAddress.getByName(value).address
}
