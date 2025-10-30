package no.nav.pensjon.simulator.tech.security.egress.oauth2.maskinporten

import no.nav.pensjon.simulator.tech.security.egress.token.EgressTokenGetter
import no.nav.pensjon.simulator.tech.security.egress.token.RawJwt
import no.nav.pensjon.simulator.tech.security.egress.token.TokenAccessParameter
import no.nav.pensjon.simulator.tech.web.WebClientBase
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient

@Service
class MaskinportenTokenService(
    val webClientBuilder: WebClient.Builder,
    val maskinportenClient: MaskinportenRequestClient,
) : EgressTokenGetter {

    override fun getEgressToken(ingressToken: String, audience: String, user: String): RawJwt {
        val accessParameter = TokenAccessParameter.jwtBearer("urn:ietf:params:oauth:grant-type:jwt-bearer")
        val tokenValue = maskinportenClient.getTokenData(accessParameter = accessParameter, scope = audience, user = user)
            .accessToken
        return RawJwt(tokenValue)
    }
}