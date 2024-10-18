package no.nav.pensjon.simulator.testutil

import no.nav.pensjon.simulator.generelt.organisasjon.Organisasjonsnummer
import no.nav.pensjon.simulator.person.Pid
import org.springframework.security.oauth2.jwt.Jwt

object TestObjects {
    val jwt = Jwt("j.w.t", null, null, mapOf("k" to "v"), mapOf("k" to "v"))

    val organisasjonsnummer = Organisasjonsnummer("123456789")

    val pid = Pid("12345678910")
}
