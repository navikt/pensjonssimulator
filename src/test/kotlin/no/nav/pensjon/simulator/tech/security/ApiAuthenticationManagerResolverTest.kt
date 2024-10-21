package no.nav.pensjon.simulator.tech.security

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import jakarta.servlet.http.HttpServletRequest
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.security.authentication.ProviderManager
import org.springframework.security.core.Authentication

class ApiAuthenticationManagerResolverTest : FunSpec({

    test("resolve uses Entra ID when path is /api/nav/**") {
        val (entraProvider, entraAuthentication) = arrangeAuth(isAuthenticated = true)
        val (maskinportenProvider, _) = arrangeAuth(isAuthenticated = false)
        val request = arrangeRequest(path = "/api/nav/service1")
        val resolver = ApiAuthenticationManagerResolver(entraProvider, maskinportenProvider)

        val authenticationManager = resolver.resolve(request)

        authenticationManager.authenticate(entraAuthentication).isAuthenticated shouldBe true
    }

    test("resolve uses Maskinporten when path is /api/tpo/**") {
        val (entraProvider, _) = arrangeAuth(isAuthenticated = false)
        val (maskinportenProvider, maskinportenAuthentication) = arrangeAuth(isAuthenticated = true)
        val request = arrangeRequest(path = "/api/tpo/service1")
        val resolver = ApiAuthenticationManagerResolver(entraProvider, maskinportenProvider)

        val authenticationManager = resolver.resolve(request)

        authenticationManager.authenticate(maskinportenAuthentication).isAuthenticated shouldBe true
    }
})

private fun arrangeAuth(isAuthenticated: Boolean): Pair<ProviderManager, Authentication> {
    val authentication = mock(Authentication::class.java).also {
        `when`(it.isAuthenticated).thenReturn(isAuthenticated)
    }

    val provider = mock(ProviderManager::class.java).also {
        `when`(it.authenticate(authentication)).thenReturn(authentication)
    }

    return Pair(provider, authentication)
}

private fun arrangeRequest(path: String): HttpServletRequest =
    mock(HttpServletRequest::class.java).also {
        `when`(it.servletPath).thenReturn(path)
    }
