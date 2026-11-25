package no.nav.pensjon.simulator.tech.security.egress.oauth2.onbehalfof

import no.nav.pensjon.simulator.tech.sporing.context.SecurityContextClaimExtractor
import org.springframework.stereotype.Component

@Component
class SecurityContextAnsattIdExtractor {

    fun id(): String = idFromSecurityContext() ?: ""

    companion object {
        const val CLAIM_KEY = "NAVident"

        private fun idFromSecurityContext(): String? = SecurityContextClaimExtractor.claim(CLAIM_KEY) as? String
    }
}