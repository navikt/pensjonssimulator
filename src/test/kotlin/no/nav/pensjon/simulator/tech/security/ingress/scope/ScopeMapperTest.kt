package no.nav.pensjon.simulator.tech.security.ingress.scope

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.springframework.security.core.authority.SimpleGrantedAuthority

class ScopeMapperTest : FunSpec({

    test("should not map unknown scope to any granted authority") {
        ScopeMapper.tokenScopeToAuthorities(
            tokenScope = "nav:pensjon/simulering/*"
        ) shouldBe emptyList()
    }

    test("should map 'pensjon/simulering:alderspensjonogprivatafp' to granted 'alderspensjon-og-privat-afp' authority") {
        ScopeMapper.tokenScopeToAuthorities(
            tokenScope = "nav:pensjon/simulering/alderspensjonogprivatafp"
        ) shouldBe listOf(SimpleGrantedAuthority("scope:simuler-alderspensjon-og-privat-afp"))
    }

    test("should map 'pensjon/simulering.read' to granted 'alderspensjon' authority") {
        ScopeMapper.tokenScopeToAuthorities(
            tokenScope = "nav:pensjon/simulering.read"
        ) shouldBe listOf(SimpleGrantedAuthority("scope:simuler-alderspensjon"))
    }

    test("should map 'pensjon/v3/alderspensjon' to granted 'alderspensjon' authority") {
        ScopeMapper.tokenScopeToAuthorities(
            tokenScope = "nav:pensjon/v3/alderspensjon"
        ) shouldBe listOf(SimpleGrantedAuthority("scope:simuler-alderspensjon"))
    }

    test("should map 'pensjonssimulator:simulering' to granted 'alderspensjon' authority") {
        ScopeMapper.tokenScopeToAuthorities(
            tokenScope = "nav:pensjonssimulator:simulering"
        ) shouldBe listOf(SimpleGrantedAuthority("scope:simuler-alderspensjon"))
    }
})
