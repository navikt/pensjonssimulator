package no.nav.pensjon.simulator.tech.security.egress.oauth2.tokenexchange.client

import no.nav.pensjon.simulator.tech.security.egress.token.CacheKey
import no.nav.pensjon.simulator.tech.security.egress.token.TokenAccessParameter
import no.nav.pensjon.simulator.tech.security.egress.token.TokenData

interface TokenExchangeClient {

    /**
     * Exhanges the token given in accessParameter.subjectToken.
     * The new token is available as TokenData.accessToken.
     */
    fun exchange(accessParameter: TokenAccessParameter, cacheKey: CacheKey): TokenData
}