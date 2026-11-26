package no.nav.pensjon.simulator.tech.security.egress

import no.nav.pensjon.simulator.tech.representasjon.RepresentertRolle
import no.nav.pensjon.simulator.tech.security.egress.config.EgressTokenSuppliersByService
import no.nav.pensjon.simulator.tech.security.egress.oauth2.onbehalfof.SecurityContextAnsattIdExtractor
import no.nav.pensjon.simulator.tech.security.ingress.SecurityContextPidExtractor
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component

@Component
class SecurityContextEnricher(
    val tokenSuppliers: EgressTokenSuppliersByService,
    val pidExtractor: SecurityContextPidExtractor,
    val ansattIdExtractor: SecurityContextAnsattIdExtractor
) {
    fun enrichAuthentication() {
        with(SecurityContextHolder.getContext()) {
            authentication = authentication?.let(::enrich)
        }
    }

    private fun enrich(auth: Authentication) =
        EnrichedAuthentication(
            initialAuth = auth,
            egressTokenSuppliersByService = tokenSuppliers,
            rolle = personenSelv() ?: underVeiledning() ?: RepresentertRolle.NONE
        )

    private fun personenSelv(): RepresentertRolle? =
        pidExtractor.pid()?.let { RepresentertRolle.SELV }

    private fun underVeiledning(): RepresentertRolle? =
        ansattIdExtractor.id()
            .takeIf { it.isNotEmpty() }
            ?.let { RepresentertRolle.UNDER_VEILEDNING }
}