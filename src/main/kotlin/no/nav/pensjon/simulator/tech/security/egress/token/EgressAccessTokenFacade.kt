package no.nav.pensjon.simulator.tech.security.egress.token

import no.nav.pensjon.simulator.tech.representasjon.RepresentertRolle
import no.nav.pensjon.simulator.tech.security.egress.AuthType
import no.nav.pensjon.simulator.tech.security.egress.config.EgressTokenTypeDecisionBasis
import no.nav.pensjon.simulator.tech.security.egress.oauth2.clientcred.ClientCredentialsEgressTokenService
import no.nav.pensjon.simulator.tech.security.egress.oauth2.maskinporten.MaskinportenTokenService
import no.nav.pensjon.simulator.tech.security.egress.oauth2.onbehalfof.OnBehalfOfEgressTokenService
import no.nav.pensjon.simulator.tech.security.egress.oauth2.tokenexchange.TokenExchangeService
import org.springframework.stereotype.Component

@Component
class EgressAccessTokenFacade(
    private val clientCredentialsTokenService: ClientCredentialsEgressTokenService,
    private val tokenExchangeService: TokenExchangeService,
    private val onBehalfOfTokenService: OnBehalfOfEgressTokenService,
    private val maskinportenTokenService: MaskinportenTokenService
) {
    fun getAccessToken(
        authType: AuthType,
        audience: String,
        tokenTypeDecisionBasis: EgressTokenTypeDecisionBasis
    ): RawJwt =
        tokenGetter(authType, tokenTypeDecisionBasis.rolle)
            .getEgressToken(ingressToken = tokenTypeDecisionBasis.ingressToken ?: "", audience)

    private fun tokenGetter(authType: AuthType, rolle: RepresentertRolle): EgressTokenGetter =
        when (authType) {
            AuthType.MACHINE_INSIDE_NAV -> clientCredentialsTokenService
            AuthType.MACHINE_OUTSIDE_NAV -> maskinportenTokenService
            AuthType.PERSONAL -> tokenGetter(rolle, stoetterTokenX = true)
            AuthType.PERSONAL_NOT_TOKEN_X -> tokenGetter(rolle, stoetterTokenX = false)
            AuthType.NONE -> NoTokenGetter()
        }

    private fun tokenGetter(rolle: RepresentertRolle, stoetterTokenX: Boolean): EgressTokenGetter =
        when (rolle) {
            RepresentertRolle.UNDER_VEILEDNING -> onBehalfOfTokenService
            RepresentertRolle.NONE -> clientCredentialsTokenService
            else -> if (stoetterTokenX) tokenExchangeService else clientCredentialsTokenService
            // NB: Hvis bruker er fullmektig vil ingress-token-typen være 'client credentials'
        }
}