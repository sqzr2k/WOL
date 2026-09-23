package de.sqzr2k.wol.domain.wol

object WolPacket {
    fun build(macAddress: String, secureOnPassword: String? = null): ByteArray {
        val mac = requireNotNull(MacAddress.bytes(macAddress)) { "Invalid MAC address" }
        val secureOn = secureOnPassword?.takeIf { it.isNotBlank() }?.let {
            requireNotNull(MacAddress.bytes(it)) { "Invalid SecureOn password" }
        }
        return ByteArray(6 + (16 * 6) + (secureOn?.size ?: 0)).also { packet ->
            packet.fill(0xFF.toByte(), 0, 6)
            repeat(16) { index -> mac.copyInto(packet, 6 + index * 6) }
            secureOn?.copyInto(packet, 102)
        }
    }
}
