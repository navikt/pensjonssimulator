package no.nav.pensjon.simulator.tech.security

import jakarta.servlet.http.HttpServletRequest
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.AuthenticationManagerResolver
import org.springframework.security.authentication.ProviderManager
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher
import org.springframework.security.web.util.matcher.RequestMatcher

class ApiAuthenticationManagerResolver(
    entraProvider: ProviderManager,
    maskinportenProvider: ProviderManager
) : AuthenticationManagerResolver<HttpServletRequest> {
    private val builder = PathPatternRequestMatcher.withDefaults()

    private val managerMap: Map<RequestMatcher, AuthenticationManager> =
        mapOf(
            builder.matcher("/api/anonym/**") to entraProvider,
            builder.matcher("/api/nav/**") to entraProvider,
            builder.matcher("/api/tpo/**") to entraProvider,
            builder.matcher("/api/v0/simuler-afp-etterfulgt-av-alderspensjon") to maskinportenProvider,
            builder.matcher("/api/v3/simuler-alderspensjon-privat-afp") to maskinportenProvider,
            builder.matcher("/api/v3/simuler-alderspensjon") to maskinportenProvider,
            builder.matcher("/api/v4/simuler-alderspensjon") to maskinportenProvider,
            builder.matcher("/api/v1/simuler-folketrygdbeholdning") to maskinportenProvider,
            builder.matcher("/api/v0/simuler-folketrygdberegnet-afp") to maskinportenProvider,
            builder.matcher("/api/v1/tidligst-mulig-uttak") to maskinportenProvider
        )

    override fun resolve(context: HttpServletRequest): AuthenticationManager =
        managerMap.entries.firstOrNull { it.key.matches(context) }?.value
            ?: throw IllegalArgumentException("Unable to resolve AuthenticationManager")
}
