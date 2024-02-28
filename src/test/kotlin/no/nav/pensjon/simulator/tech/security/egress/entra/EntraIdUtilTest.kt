package no.nav.pensjon.simulator.tech.security.egress.entra

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

class EntraIdUtilTest {

    @Test
    fun `getDefaultScope returns the default scope for a given service`() {
        val scope = EntraIdUtil.getDefaultScope("cluster1:namespace1:app1")
        assertEquals("api://cluster1.namespace1.app1/.default", scope)
    }
}
