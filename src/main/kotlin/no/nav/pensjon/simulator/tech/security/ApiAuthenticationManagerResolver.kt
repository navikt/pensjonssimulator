package no.nav.pensjon.simulator.tech.security

import jakarta.servlet.http.HttpServletRequest
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.AuthenticationManagerResolver
import org.springframework.security.authentication.ProviderManager
import org.springframework.security.web.util.matcher.AntPathRequestMatcher
import org.springframework.security.web.util.matcher.RequestMatcher

class ApiAuthenticationManagerResolver(
    entraProvider: ProviderManager,
    maskinportenProvider: ProviderManager
) : AuthenticationManagerResolver<HttpServletRequest> {
    private val managerMap: Map<RequestMatcher, AuthenticationManager> =
        mapOf(
            AntPathRequestMatcher("/api/anonym/**") to entraProvider,
            AntPathRequestMatcher("/api/nav/**") to entraProvider,
            AntPathRequestMatcher("/api/tpo/**") to entraProvider,
            AntPathRequestMatcher("/api/v4/simuler-alderspensjon") to maskinportenProvider,
            AntPathRequestMatcher("/api/v1/simuler-folketrygdbeholdning") to maskinportenProvider,
            AntPathRequestMatcher("/api/v1/tidligst-mulig-uttak") to maskinportenProvider
        )

    override fun resolve(request: HttpServletRequest?): AuthenticationManager =
        managerMap.entries.firstOrNull { it.key.matches(request) }?.value
            ?: throw IllegalArgumentException("Unable to resolve AuthenticationManager")
}
