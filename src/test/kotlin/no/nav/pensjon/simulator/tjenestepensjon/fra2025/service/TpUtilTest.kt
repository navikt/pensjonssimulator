package no.nav.pensjon.simulator.tjenestepensjon.fra2025.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test


class TpUtilTest {

    @Test
    fun `redact maskerer fnr`() {
        assertEquals("***********", TpUtil.redact("02345678901"))
    }

    @Test
    fun `redact maskerer alle fnr i en tekst`() {
        assertEquals("ident:***********fnr:***********pid:***********", TpUtil.redact("ident:22345600001fnr:12345600001pid:01410199999"))
    }

    @Test
    fun `redact maskerer fnr i en tekst med mellomrom`() {
        assertEquals("ident:*********** fnr:*********** pid:***********", TpUtil.redact("ident:22345600001 fnr:12345600001 pid:01410199999"))
    }

    @Test
    fun `redact endrer ikke tekst med desimaltall`() {
        val tekst = "ident:2234.5600001 fnr:1.2345600001 pid:0141019999.9"
        assertEquals(tekst, TpUtil.redact(tekst))
    }

    @Test
    fun `redact maskerer fnr i en JSON-request`() {
        val jsonRequest = """
            {
                "ident": "22345600001",
                "fnr":"12345600001",
                "pid": "01410199999"
            }
        """.trimIndent()
        val expectedRedacted = """
            {
                "ident": "***********",
                "fnr":"***********",
                "pid": "***********"
            }
        """.trimIndent()
        assertEquals(expectedRedacted, TpUtil.redact(jsonRequest))
    }
}
