package no.nav.pensjon.simulator.tech.security.egress.token

class NoTokenGetter : EgressTokenGetter {

    override fun getEgressToken(ingressToken: String, audience: String) =
        RawJwt(value = "")
}