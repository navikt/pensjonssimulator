package no.nav.pensjon.simulator.tech.security.egress

import no.nav.pensjon.simulator.tech.security.egress.config.EgressService
import no.nav.pensjon.simulator.tech.security.egress.config.EgressTokenTypeDecisionBasis
import no.nav.pensjon.simulator.tech.security.egress.token.RawJwt
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt

object EgressAccess {

    fun token(service: EgressService): RawJwt =
        SecurityContextHolder.getContext().authentication?.enriched()?.let {
            token(authentication = it, service)
        } ?: throw AuthenticationCredentialsNotFoundException("failed to get egress access token")

    private fun token(
        authentication: EnrichedAuthentication,
        service: EgressService
    ): RawJwt =
        authentication.getEgressAccessToken(
            service,
            tokenTypeDecisionBasis = tokenTypeDecisionBasis(authentication)
        )

    private fun tokenTypeDecisionBasis(authentication: EnrichedAuthentication) =
        EgressTokenTypeDecisionBasis(
            rolle = authentication.rolle,
            ingressToken = (authentication.credentials as? Jwt)?.tokenValue
        )
}