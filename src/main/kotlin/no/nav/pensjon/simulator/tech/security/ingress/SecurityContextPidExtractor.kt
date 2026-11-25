package no.nav.pensjon.simulator.tech.security.ingress

import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.tech.sporing.context.SecurityContextClaimExtractor
import org.springframework.stereotype.Component

@Component
class SecurityContextPidExtractor {

    fun pid(): Pid? = pidFromSecurityContext()?.let(::Pid)

    private companion object {
        private const val CLAIM_KEY = "pid"

        private fun pidFromSecurityContext(): String? = SecurityContextClaimExtractor.claim(CLAIM_KEY) as? String
    }
}