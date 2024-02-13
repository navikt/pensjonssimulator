package no.nav.pensjon.simulator.tech.security.ingress

import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import no.nav.pensjon.simulator.tech.security.egress.SecurityContextEnricher
import org.springframework.web.filter.GenericFilterBean

/**
 * Servlet filter which augments Spring Security authentication data as required by the application.
 */
class AuthenticationEnricherFilter(private val enricher: SecurityContextEnricher) : GenericFilterBean() {

    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        enricher.enrichAuthentication(request as HttpServletRequest)
        chain.doFilter(request, response)
    }
}
