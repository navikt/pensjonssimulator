package no.nav.pensjon.simulator.tech.security

/* These tests do not work with PathPatternRequestMatcher

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import jakarta.servlet.http.HttpServletRequest
import org.springframework.security.authentication.ProviderManager
import org.springframework.security.core.Authentication

class ApiAuthenticationManagerResolverTest : FunSpec({

    test("'resolve' should use Entra ID when path is /api/nav/**") {
        val (entraProvider, entraAuthentication) = arrangeAuth(authenticated = true)
        val (maskinportenProvider, _) = arrangeAuth(authenticated = false)
        val request = arrangeRequest(path = "/api/nav/service1")
        val resolver = ApiAuthenticationManagerResolver(entraProvider, maskinportenProvider)

        val authenticationManager = resolver.resolve(request)

        authenticationManager.authenticate(entraAuthentication).isAuthenticated shouldBe true
    }

    test("'resolve' should use Entra ID when path is /api/tpo/**") {
        val (entraProvider, entraAuthentication) = arrangeAuth(authenticated = true)
        val (maskinportenProvider, _) = arrangeAuth(authenticated = false)
        val request = arrangeRequest(path = "/api/tpo/service1")
        val resolver = ApiAuthenticationManagerResolver(entraProvider, maskinportenProvider)

        val authenticationManager = resolver.resolve(request)

        authenticationManager.authenticate(entraAuthentication).isAuthenticated shouldBe true
    }

    test("'resolve' should use Maskinporten when path is /api/v4/simuler-alderspensjon") {
        val (entraProvider, _) = arrangeAuth(authenticated = false)
        val (maskinportenProvider, maskinportenAuthentication) = arrangeAuth(authenticated = true)
        val request = arrangeRequest(path = "/api/v4/simuler-alderspensjon")
        val resolver = ApiAuthenticationManagerResolver(entraProvider, maskinportenProvider)

        val authenticationManager = resolver.resolve(request)

        authenticationManager.authenticate(maskinportenAuthentication).isAuthenticated shouldBe true
    }
})

private fun arrangeAuth(authenticated: Boolean): Pair<ProviderManager, Authentication> {
    val authentication = mockk<Authentication>().apply {
        every { isAuthenticated } returns authenticated
    }

    val provider = mockk<ProviderManager>().apply {
        every { authenticate(authentication) } returns authentication
    }

    return Pair(provider, authentication)
}

private fun arrangeRequest(path: String): HttpServletRequest =
    mockk<HttpServletRequest>(relaxed = true).apply {
        every { servletPath } returns path
    }
*/