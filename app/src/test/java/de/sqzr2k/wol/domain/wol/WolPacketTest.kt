package de.sqzr2k.wol.domain.wol

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

class WolPacketTest {
    @Test
    fun `standard packet contains prefix and sixteen mac copies`() {
        val mac = byteArrayOf(0x02, 0x00, 0x00, 0x00, 0x00, 0x01)
        val packet = WolPacket.build("02:00:00:00:00:01")
        assertEquals(102, packet.size)
        assertArrayEquals(ByteArray(6) { 0xFF.toByte() }, packet.copyOfRange(0, 6))
        repeat(16) { assertArrayEquals(mac, packet.copyOfRange(6 + it * 6, 12 + it * 6)) }
    }

    @Test
    fun `secure on appends six bytes`() {
        val packet = WolPacket.build("02:00:00:00:00:01", "01:02:03:04:05:06")
        assertEquals(108, packet.size)
        assertArrayEquals(
            byteArrayOf(0x01, 0x02, 0x03, 0x04, 0x05, 0x06),
            packet.copyOfRange(102, 108),
        )
    }
}
