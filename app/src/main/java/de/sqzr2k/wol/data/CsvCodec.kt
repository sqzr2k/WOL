package de.sqzr2k.wol.data

import de.sqzr2k.wol.data.local.DeviceEntity
import de.sqzr2k.wol.data.local.GroupEntity
import de.sqzr2k.wol.domain.wol.MacAddress

data class CsvSnapshot(val devices: List<DeviceEntity>, val groups: List<GroupEntity>)

object CsvCodec {
    private val header = listOf(
        "id", "name", "macAddress", "group", "color", "hostname", "deviceIp",
        "broadcastAddress", "port", "secureOnPassword", "wifiSsid", "pingCheck",
        "netbiosCheck", "manualPort",
    )

    fun encode(devices: List<DeviceEntity>, groups: List<GroupEntity>): String {
        val groupNames = groups.associate { it.id to it.name }
        return buildString {
            appendLine(header.joinToString(","))
            devices.forEach { device ->
                appendLine(
                    listOf(
                        device.id.toString(), device.name, device.macAddress,
                        device.groupId?.let(groupNames::get).orEmpty(), device.color.toString(),
                        device.hostname.orEmpty(), device.deviceIp.orEmpty(), device.broadcastAddress.orEmpty(),
                        device.port.toString(), device.secureOnPassword.orEmpty(), device.wifiSsid.orEmpty(),
                        device.pingCheckEnabled.toString(), device.netbiosCheckEnabled.toString(),
                        device.manualPort?.toString().orEmpty(),
                    ).joinToString(",", transform = ::escape),
                )
            }
        }
    }

    fun decode(csv: String): CsvSnapshot {
        val rows = parseRows(csv)
        require(rows.isNotEmpty() && rows.first() == header) { "CSV-Kopfzeile ist ungültig" }
        val groups = linkedMapOf<String, GroupEntity>()
        val devices = rows.drop(1).filter { it.any(String::isNotBlank) }.mapIndexed { index, row ->
            require(row.size == header.size) { "Zeile ${index + 2} hat ${row.size} statt ${header.size} Spalten" }
            val mac = requireNotNull(MacAddress.normalize(row[2])) { "Ungültige MAC-Adresse in Zeile ${index + 2}" }
            val groupName = row[3].trim()
            if (groupName.isNotEmpty()) groups.getOrPut(groupName.lowercase()) {
                GroupEntity(id = groups.size.toLong() + 1, name = groupName, sortOrder = groups.size)
            }
            DeviceEntity(
                id = row[0].toLongOrNull() ?: 0,
                name = row[1].trim().also { require(it.isNotEmpty()) { "Gerätename fehlt in Zeile ${index + 2}" } },
                macAddress = mac,
                groupId = groupName.takeIf(String::isNotEmpty)?.let { groups[it.lowercase()]?.id },
                color = row[4].toLongOrNull() ?: 0xFF315DA8,
                hostname = row[5].ifBlank { null },
                deviceIp = row[6].ifBlank { null },
                broadcastAddress = row[7].ifBlank { null },
                port = row[8].toIntOrNull()?.takeIf { it in 1..65535 } ?: error("Ungültiger UDP-Port in Zeile ${index + 2}"),
                secureOnPassword = row[9].ifBlank { null }?.let {
                    requireNotNull(MacAddress.normalizeSecureOn(it)) { "Ungültiges SecureOn-Passwort in Zeile ${index + 2}" }
                },
                wifiSsid = row[10].ifBlank { null },
                pingCheckEnabled = row[11].toBooleanStrictOrNull() ?: false,
                netbiosCheckEnabled = row[12].toBooleanStrictOrNull() ?: false,
                manualPort = row[13].ifBlank { null }?.toIntOrNull()?.takeIf { it in 1..65535 },
            )
        }
        return CsvSnapshot(devices, groups.values.toList())
    }

    private fun escape(value: String): String = if (value.any { it == ',' || it == '"' || it == '\n' || it == '\r' }) {
        "\"${value.replace("\"", "\"\"")}\""
    } else value

    private fun parseRows(text: String): List<List<String>> {
        val rows = mutableListOf<MutableList<String>>()
        var row = mutableListOf<String>()
        val field = StringBuilder()
        var quoted = false
        var i = 0
        while (i < text.length) {
            val char = text[i]
            when {
                quoted && char == '"' && i + 1 < text.length && text[i + 1] == '"' -> { field.append('"'); i++ }
                char == '"' -> quoted = !quoted
                !quoted && char == ',' -> { row += field.toString(); field.clear() }
                !quoted && (char == '\n' || char == '\r') -> {
                    if (char == '\r' && i + 1 < text.length && text[i + 1] == '\n') i++
                    row += field.toString(); field.clear(); rows += row; row = mutableListOf()
                }
                else -> field.append(char)
            }
            i++
        }
        require(!quoted) { "Nicht geschlossenes Anführungszeichen" }
        if (field.isNotEmpty() || row.isNotEmpty()) { row += field.toString(); rows += row }
        return rows
    }
}
