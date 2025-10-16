package no.nav.pensjon.simulator.tech.security.ingress.jwt

import no.nav.pensjon.simulator.tech.security.ingress.claim.ConsumerClaimUtil
import no.nav.pensjon.simulator.tech.security.ingress.scope.ScopeMapper.tokenScopeToAuthorities
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.util.StringUtils.hasText

fun Jwt.authoritiesFromScope(): Collection<GrantedAuthority> =
    this.collectionClaim("scope")
        .flatMap(::tokenScopeToAuthorities)

fun Jwt.consumerOrganisasjonsnummer(): String =
    this.organisasjonsnummer(claimName = "consumer")
        ?: throw object : AuthenticationException("Maskinporten-token mangler 'consumer' claim") {}

fun Jwt.supplierOrganisasjonsnummer(): String? =
    this.organisasjonsnummer(claimName = "supplier")

private fun Jwt.collectionClaim(claimName: String): Collection<String> =
    when (val it = getClaim<Any>(claimName)) {
        is String -> if (hasText(it)) it.split(" ") else emptyList()
        is Collection<*> -> it.filterIsInstance<String>()
        else -> emptyList()
    }

private fun Jwt.organisasjonsnummer(claimName: String): String? =
    this.getClaimAsMap(claimName)?.let(ConsumerClaimUtil::organisasjonsnummer)
