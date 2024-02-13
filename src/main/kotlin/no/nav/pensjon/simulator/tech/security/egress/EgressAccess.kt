package no.nav.pensjon.simulator.tech.security.egress

import no.nav.pensjon.simulator.tech.security.egress.config.EgressService
import no.nav.pensjon.simulator.tech.security.egress.token.RawJwt
import org.springframework.security.core.context.SecurityContextHolder

object EgressAccess {

    fun token(service: EgressService): RawJwt =
        SecurityContextHolder.getContext().authentication.enriched().getEgressAccessToken(service)
}
