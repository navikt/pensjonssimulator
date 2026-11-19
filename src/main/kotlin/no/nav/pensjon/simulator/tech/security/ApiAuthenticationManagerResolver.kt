package no.nav.pensjon.simulator.tech.security

import jakarta.servlet.http.HttpServletRequest
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.AuthenticationManagerResolver
import org.springframework.security.authentication.ProviderManager
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher
import org.springframework.security.web.util.matcher.RequestMatcher

class ApiAuthenticationManagerResolver(
    navInternManager: ProviderManager,
    maskinportenManager: ProviderManager
) : AuthenticationManagerResolver<HttpServletRequest> {
    private val builder = PathPatternRequestMatcher.withDefaults()

    private val managersByMatcher: Map<RequestMatcher, AuthenticationManager> =
        mapOf(
            builder.matcher("/api/anonym/**") to navInternManager,
            builder.matcher("/api/nav/**") to navInternManager,
            builder.matcher("/api/tpo/**") to navInternManager,
            builder.matcher("/api/v0/simuler-afp-etterfulgt-av-alderspensjon") to maskinportenManager,
            builder.matcher("/api/v2/simuler-alderspensjon-privat-afp") to maskinportenManager,
            builder.matcher("/api/v3/simuler-alderspensjon-privat-afp") to maskinportenManager,
            builder.matcher("/api/v3/simuler-alderspensjon") to maskinportenManager,
            builder.matcher("/api/v4/simuler-alderspensjon") to maskinportenManager,
            builder.matcher("/api/v1/simuler-folketrygdbeholdning") to maskinportenManager,
            builder.matcher("/api/v0/simuler-folketrygdberegnet-afp") to maskinportenManager,
            builder.matcher("/api/v1/tidligst-mulig-uttak") to maskinportenManager
        )

    override fun resolve(context: HttpServletRequest): AuthenticationManager =
        managersByMatcher.entries.firstOrNull { it.key.matches(context) }?.value
            ?: throw IllegalArgumentException("Unable to resolve AuthenticationManager")
}