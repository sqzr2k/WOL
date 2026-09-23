package de.sqzr2k.wol.domain.wol

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MacAddressTest {
    @Test
    fun `normalizes common formats`() {
        val expected = "02:00:00:00:00:01"
        assertEquals(expected, MacAddress.normalize("02:00:00:00:00:01"))
        assertEquals(expected, MacAddress.normalize("02-00-00-00-00-01"))
        assertEquals(expected, MacAddress.normalize("020000000001"))
    }

    @Test
    fun `rejects malformed address`() {
        assertNull(MacAddress.normalize("02:00:00"))
        assertNull(MacAddress.normalize("GG:00:00:00:00:01"))
    }
}
