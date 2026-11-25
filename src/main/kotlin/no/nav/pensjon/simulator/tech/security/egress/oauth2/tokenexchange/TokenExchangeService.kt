package no.nav.pensjon.simulator.tech.security.egress.oauth2.tokenexchange

import no.nav.pensjon.simulator.tech.security.egress.oauth2.tokenexchange.client.TokenExchangeClient
import no.nav.pensjon.simulator.tech.security.egress.token.CacheKey
import no.nav.pensjon.simulator.tech.security.egress.token.EgressTokenGetter
import no.nav.pensjon.simulator.tech.security.egress.token.RawJwt
import no.nav.pensjon.simulator.tech.security.egress.token.TokenAccessParameter
import no.nav.pensjon.simulator.tech.security.egress.token.TokenData
import no.nav.pensjon.simulator.tech.security.ingress.SecurityContextPidExtractor
import org.springframework.stereotype.Service

@Service
class TokenExchangeService(
    private val exchanger: TokenExchangeClient,
    private val authorizedPidProvider: SecurityContextPidExtractor
) : EgressTokenGetter {

    override fun getEgressToken(ingressToken: String, audience: String) =
        RawJwt(
            value = egressTokenData(ingressToken, audience).accessToken
        )

    private fun egressTokenData(ingressToken: String, audience: String): TokenData =
        authorizedPidProvider.pid()?.let {
            exchanger.exchange(
                accessParameter = TokenAccessParameter.tokenExchange(subjectToken = ingressToken),
                cacheKey = CacheKey(scope = audience, pid = it.value)
            )
        } ?: throw IllegalStateException("TokenExchangeService melder om manglende PID i ingress-token")
}