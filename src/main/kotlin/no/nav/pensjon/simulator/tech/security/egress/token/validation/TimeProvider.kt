package no.nav.pensjon.simulator.tech.security.egress.token.validation

import java.time.LocalDateTime

fun interface TimeProvider {
    fun time(): LocalDateTime
}
