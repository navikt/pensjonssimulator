package no.nav.pensjon.simulator.tech.security.ingress.scope

import mu.KotlinLogging
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority

object ScopeMapper {

    private val log = KotlinLogging.logger {}

    fun tokenScopeToAuthorities(tokenScope: String): Collection<GrantedAuthority> =
        when (tokenScope) {
            "nav:pensjon/simulering/alderspensjonogprivatafp" -> authorities(internalScope = "simuler-alderspensjon-og-privat-afp")
            "nav:pensjon/simulering.read" -> authorities(internalScope = "simuler-alderspensjon")
            "nav:pensjon/v3/alderspensjon" -> authorities(internalScope = "simuler-alderspensjon")
            "nav:pensjonssimulator:simulering" -> authorities(internalScope = "simuler-alderspensjon")
            else -> emptyList<GrantedAuthority>().also { log.info { "Ugyldig scope '$tokenScope'" } }
        }

    private fun authorities(internalScope: String): List<SimpleGrantedAuthority> =
        listOf(scopeBasedAuthority(internalScope))

    private fun scopeBasedAuthority(internalScope: String) =
        SimpleGrantedAuthority("scope:$internalScope")
}