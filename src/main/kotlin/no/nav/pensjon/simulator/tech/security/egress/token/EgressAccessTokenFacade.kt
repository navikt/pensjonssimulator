package no.nav.pensjon.simulator.tech.security.egress.token

import no.nav.pensjon.simulator.tech.security.egress.AuthType
import no.nav.pensjon.simulator.tech.security.egress.oauth2.clientcred.ClientCredentialsEgressTokenService
import no.nav.pensjon.simulator.tech.security.egress.oauth2.maskinporten.MaskinportenTokenService
import org.springframework.stereotype.Component

@Component
class EgressAccessTokenFacade(
    private val clientCredentialsTokenService: ClientCredentialsEgressTokenService,
    private val maskinportenTokenService: MaskinportenTokenService
) {

    fun getAccessToken(authType: AuthType, audience: String): RawJwt =
        tokenGetter(authType).getEgressToken("", audience, "")

    private fun tokenGetter(authType: AuthType): EgressTokenGetter =
        when (authType) {
            AuthType.MACHINE_INSIDE_NAV -> clientCredentialsTokenService
            AuthType.MACHINE_OUTSIDE_NAV -> maskinportenTokenService
            else -> unsupported(authType)
        }

    companion object {
        private fun <T> unsupported(authType: AuthType): T {
            throw IllegalArgumentException("Unsupported auth type: $authType")
        }
    }
}
