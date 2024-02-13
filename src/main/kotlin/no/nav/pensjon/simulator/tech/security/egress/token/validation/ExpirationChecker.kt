package no.nav.pensjon.simulator.tech.security.egress.token.validation

import java.time.LocalDateTime

interface ExpirationChecker {

    fun isExpired(issuedTime: LocalDateTime, expiresInSeconds: Long): Boolean

    fun time(): LocalDateTime
}
