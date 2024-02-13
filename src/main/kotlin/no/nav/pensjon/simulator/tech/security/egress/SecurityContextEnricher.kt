package no.nav.pensjon.simulator.tech.security.egress

import jakarta.servlet.http.HttpServletRequest
import no.nav.pensjon.simulator.tech.security.egress.config.EgressTokenSuppliersByService
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component

@Component
class SecurityContextEnricher(val tokenSuppliers: EgressTokenSuppliersByService) {

    fun enrichAuthentication(request: HttpServletRequest) {
        with(SecurityContextHolder.getContext()) {
            authentication = authentication?.let { EnrichedAuthentication(it, tokenSuppliers) }
        }
    }
}
