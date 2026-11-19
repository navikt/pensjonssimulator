package no.nav.pensjon.simulator.tech.security.egress.oauth2.onbehalfof

import no.nav.pensjon.simulator.tech.security.egress.entra.EntraIdUtil.getDefaultScope
import no.nav.pensjon.simulator.tech.security.egress.oauth2.onbehalfof.entra.EntraIdOnBehalfOfClient
import no.nav.pensjon.simulator.tech.security.egress.token.EgressTokenGetter
import no.nav.pensjon.simulator.tech.security.egress.token.RawJwt
import no.nav.pensjon.simulator.tech.security.egress.token.TokenAccessParameter
import no.nav.pensjon.simulator.tech.security.egress.token.TokenData
import org.springframework.stereotype.Service

@Service
class OnBehalfOfEgressTokenService(
    private val tokenGetter: EntraIdOnBehalfOfClient,
    private val ansattIdExtractor: SecurityContextAnsattIdExtractor
) : EgressTokenGetter {

    override fun getEgressToken(ingressToken: String, audience: String) =
        RawJwt(
            value = egressTokenData(ingressToken, audience).accessToken
        )

    private fun egressTokenData(ingressToken: String, audience: String): TokenData =
        tokenGetter.getTokenData(
            accessParameter = TokenAccessParameter.jwtBearer(assertion = ingressToken),
            scope = getDefaultScope(fullyQualifiedApplicationName = audience),
            user = ansattIdExtractor.id()
        )
}