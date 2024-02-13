package no.nav.pensjon.simulator.tech.security.egress.token

interface TokenDataGetter {
    fun getTokenData(accessParameter: TokenAccessParameter, audience: String): TokenData
}
