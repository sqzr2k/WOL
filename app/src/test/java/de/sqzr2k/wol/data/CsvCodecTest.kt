package de.sqzr2k.wol.data

import de.sqzr2k.wol.data.local.DeviceEntity
import de.sqzr2k.wol.data.local.GroupEntity
import org.junit.Assert.assertEquals
import org.junit.Test

class CsvCodecTest {
    @Test
    fun `round trip preserves relevant fields and escaping`() {
        val groups = listOf(GroupEntity(7, "Office, \"upstairs\"", 0))
        val devices = listOf(
            DeviceEntity(
                id = 42, name = "Example device", macAddress = "02:00:00:00:00:01", groupId = 7,
                color = 1234, hostname = "device.example", deviceIp = "192.0.2.50",
                broadcastAddress = "192.0.2.255", port = 9, secureOnPassword = "01:02:03:04:05:06",
                wifiSsid = "Example\nWi-Fi", pingCheckEnabled = true, netbiosCheckEnabled = false, manualPort = 3389,
            ),
        )
        val decoded = CsvCodec.decode(CsvCodec.encode(devices, groups))
        val expected = devices.single()
        val actual = decoded.devices.single()
        assertEquals(expected.copy(groupId = 1), actual.copy(createdAt = expected.createdAt, updatedAt = expected.updatedAt))
        assertEquals(groups.single().name, decoded.groups.single().name)
    }
}
