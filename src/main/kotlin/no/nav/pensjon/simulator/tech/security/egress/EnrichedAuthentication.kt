package no.nav.pensjon.simulator.tech.security.egress

import no.nav.pensjon.simulator.tech.security.egress.config.EgressService
import no.nav.pensjon.simulator.tech.security.egress.config.EgressTokenSuppliersByService
import no.nav.pensjon.simulator.tech.security.egress.token.RawJwt
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority

/**
 * Authentication data is initially obtained by Spring Security.
 * This class augments that data by adding a mechanism for obtaining egress tokens
 * (used by backend for accessing other services).
 */
class EnrichedAuthentication(
    private val initialAuth: Authentication?,
    private val egressTokenSuppliersByService: EgressTokenSuppliersByService
) : Authentication {

    fun getEgressAccessToken(service: EgressService): RawJwt {
        return egressTokenSuppliersByService.value[service]?.get() ?: RawJwt("")
    }

    override fun getName(): String? = initialAuth?.name

    override fun getAuthorities(): MutableCollection<out GrantedAuthority>? = initialAuth?.authorities

    override fun getCredentials(): Any? = initialAuth?.credentials

    override fun getDetails(): Any? = initialAuth?.details

    override fun getPrincipal(): Any? = initialAuth?.principal

    override fun isAuthenticated(): Boolean = initialAuth?.isAuthenticated ?: false

    override fun setAuthenticated(isAuthenticated: Boolean) {
        initialAuth?.let { it.isAuthenticated = isAuthenticated }
    }
}

fun Authentication.enriched(): EnrichedAuthentication = this as EnrichedAuthentication
