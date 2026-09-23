package de.sqzr2k.wol.domain.wol

object MacAddress {
    private val separators = Regex("[:-]")
    private val hex = Regex("^[0-9A-F]{12}$")

    fun normalize(value: String): String? {
        val compact = value.trim().uppercase().replace(separators, "")
        if (!hex.matches(compact)) return null
        return compact.chunked(2).joinToString(":")
    }

    fun bytes(value: String): ByteArray? = normalize(value)
        ?.split(":")
        ?.map { it.toInt(16).toByte() }
        ?.toByteArray()

    fun normalizeSecureOn(value: String): String? = normalize(value)
}
